"""Isolated growth API -> crawler -> pgvector experiments. Never points at production.
Prepare both repositories with gradlew pipelineHarnessClasspath, then install
psycopg[binary], redis, matplotlib into build/pipeline-experiment-python.
Docker containers must be the dedicated navik-pipeline-e2e-pg/redis instances.
"""

import argparse
import base64
import hashlib
import hmac
import json
import os
import re
import socket
import struct
import subprocess
import sys
import threading
import time
import uuid
from collections import Counter
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[1]
CRAWLER = ROOT.parent / "Navik-BE-Crawler"
sys.path.insert(0, str(ROOT / "build/pipeline-experiment-python"))
import psycopg
import redis

DB = "host=127.0.0.1 port=55440 dbname=navik_pipeline_e2e user=pipeline_test"
REDIS = redis.Redis(host="127.0.0.1", port=56380, decode_responses=True, socket_timeout=3)
JWT_KEY = b"pipeline-e2e-only-not-a-production-secret-2026"
PROCS = {}
OUTPUT = ROOT / "docs/experiments/2026-09-06"
OUTPUT.mkdir(parents=True, exist_ok=True)


def emit(event, **data):
    value = dict(time=time.time(), event=event, **data)
    print(json.dumps(value, ensure_ascii=True), flush=True)
    with (OUTPUT / "events.jsonl").open("a", encoding="utf-8") as f:
        f.write(json.dumps(value) + "\n")


def sql(query, params=(), one=False):
    with psycopg.connect(DB, autocommit=True) as conn:
        cur = conn.execute(query, params)
        if not cur.description:
            return None
        rows = cur.fetchall()
        return (rows[0][0] if rows else None) if one else rows


def token(user=1):
    b64 = lambda b: base64.urlsafe_b64encode(b).rstrip(b"=")
    body = (
        b64(json.dumps({"alg": "HS256", "typ": "JWT"}).encode())
        + b"."
        + b64(
            json.dumps(
                {"sub": str(user), "auth": "ROLE_USER", "status": "ACTIVE", "exp": int(time.time()) + 3600}
            ).encode()
        )
    )
    return (body + b"." + b64(hmac.new(JWT_KEY, body, hashlib.sha256).digest())).decode()


def http(path, body=None, user=1, port=58081, timeout=20):
    headers = {"Content-Type": "application/json"}
    if user is not None:
        headers["Authorization"] = "Bearer " + token(user)
    req = Request(
        f"http://127.0.0.1:{port}" + path,
        data=None if body is None else json.dumps(body).encode(),
        headers=headers,
    )
    try:
        with urlopen(req, timeout=timeout) as response:
            return response.status, json.loads(response.read())
    except HTTPError as error:
        raw = error.read()
        try:
            return error.code, json.loads(raw)
        except ValueError:
            return error.code, raw.decode(errors="replace")[:1000]


class Provider:
    def __init__(self):
        self.lock = threading.Lock()
        self.slots = threading.Semaphore(2)
        self.reset()

    def reset(self, chat_delay=0.20, embed_delay=0.10, failure=None, slow=None):
        with getattr(self, "lock", threading.Lock()):
            self.calls = []
            self.counts = Counter()
            self.active = 0
            self.peak = 0
            self.chat_delay = chat_delay
            self.embed_delay = embed_delay
            self.failure = failure
            self.slow = slow

    def snapshot(self):
        with self.lock:
            return dict(calls=list(self.calls), counts=dict(self.counts), peak=self.peak)


PROVIDER = Provider()


class MockOpenAI(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def do_POST(self):
        if self.headers.get("Transfer-Encoding", "").lower() == "chunked":
            chunks = []
            while True:
                size = int(self.rfile.readline().split(b";")[0], 16)
                if size == 0:
                    while self.rfile.readline().strip():
                        pass
                    break
                chunks.append(self.rfile.read(size))
                self.rfile.read(2)
            raw = b"".join(chunks)
        else:
            raw = self.rfile.read(int(self.headers.get("Content-Length", "0")))
        req = json.loads(raw)
        is_chat = self.path.endswith("/chat/completions")
        if is_chat:
            prompt = " ".join(str(m.get("content", "")) for m in req["messages"] if m["role"] == "user")
            marker = re.search(r"JOB_[A-Za-z0-9_]+", prompt)
            key = marker.group(0) if marker else "UNKNOWN"
            tool_done = any(m["role"] == "tool" for m in req["messages"])
            kind = "chat_final" if tool_done else "chat_tool"
        else:
            text = req["input"]
            text = text[0] if isinstance(text, list) else text
            key = str(text)
            kind = "embedding"
        received = time.time()
        with PROVIDER.lock:
            PROVIDER.counts[kind + ":" + key] += 1
            attempt = PROVIDER.counts[kind + ":" + key]
            event = dict(kind=kind, key=key, attempt=attempt, received=received)
            PROVIDER.calls.append(event)
        with PROVIDER.slots:
            with PROVIDER.lock:
                PROVIDER.active += 1
                PROVIDER.peak = max(PROVIDER.peak, PROVIDER.active)
                event["started"] = time.time()
            try:
                time.sleep(PROVIDER.chat_delay if is_chat else PROVIDER.embed_delay)
                if PROVIDER.slow and PROVIDER.slow in key and kind == "embedding" and attempt == 1:
                    time.sleep(35)
                fail = PROVIDER.failure and PROVIDER.failure in key and kind == "embedding" and attempt == 1
                if fail:
                    status = 429
                    response = {
                        "error": {
                            "message": "injected temporary failure",
                            "type": "rate_limit_error",
                            "code": "rate_limit",
                        }
                    }
                elif is_chat:
                    if not tool_done:
                        msg = {
                            "role": "assistant",
                            "content": None,
                            "tool_calls": [
                                {
                                    "id": "call_level",
                                    "type": "function",
                                    "function": {
                                        "name": "retrieveLevelCriteria",
                                        "arguments": '{"jobId":4,"levelValue":1}',
                                    },
                                }
                            ],
                        }
                        finish = "tool_calls"
                    else:
                        result = {
                            "title": key,
                            "content": key,
                            "kpis": [{"kpiCardId": 1, "delta": 3}],
                            "abilities": [key + "_A", key + "_B"],
                        }
                        msg = {"role": "assistant", "content": json.dumps(result)}
                        finish = "stop"
                    status = 200
                    response = {
                        "id": "chat-" + uuid.uuid4().hex,
                        "object": "chat.completion",
                        "created": int(time.time()),
                        "model": "fixture",
                        "choices": [{"index": 0, "message": msg, "finish_reason": finish}],
                        "usage": {"prompt_tokens": 20, "completion_tokens": 20, "total_tokens": 40},
                    }
                else:
                    vector = [0.01] * 1536
                    value = (
                        base64.b64encode(struct.pack("<1536f", *vector)).decode()
                        if req.get("encoding_format") == "base64"
                        else vector
                    )
                    status = 200
                    response = {
                        "object": "list",
                        "model": req.get("model", "fixture"),
                        "data": [{"object": "embedding", "index": 0, "embedding": value}],
                        "usage": {"prompt_tokens": 10, "total_tokens": 10},
                    }
                raw = json.dumps(response).encode()
                self.send_response(status)
                self.send_header("Content-Type", "application/json")
                self.send_header("Content-Length", str(len(raw)))
                if status == 429:
                    self.send_header("Retry-After", "3")
                self.end_headers()
                self.wfile.write(raw)
                event["status"] = status
            except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError, OSError):
                event["status"] = "client_disconnected"
            finally:
                with PROVIDER.lock:
                    PROVIDER.active -= 1
                    event["finished"] = time.time()


def stop(name, force=False):
    proc = PROCS.pop(name, None)
    if proc and proc.poll() is None:
        if force:
            proc.kill()
        else:
            proc.terminate()
        proc.wait(timeout=15)
        emit(
            "process_stopped",
            name=name,
            pid=proc.pid,
            method="kill" if force else "terminate",
            graceful=False,
        )


def start(name, mode="stream", concurrency=2):
    stop(name)
    root = ROOT if name == "spring" else CRAWLER
    main = (
        "navik.pipeline.e2e.GrowthApiHarness"
        if name == "spring"
        else "navik.growth.pipeline.e2e.GrowthCrawlerHarness"
    )
    props = {
        "spring.config.name": "pipeline-isolated-harness",
        "spring.profiles.active": "e2e",
        "server.address": "127.0.0.1",
        "server.port": "58081" if name == "spring" else "58082",
        "spring.data.redis.host": "127.0.0.1",
        "spring.data.redis.port": "56380",
        "logging.level.root": "WARN",
        "logging.level.navik": "INFO",
        "spring.main.banner-mode": "off",
        "server.tomcat.threads.max": "24",
        "server.tomcat.threads.min-spare": "2",
    }
    if name == "spring":
        props.update(
            {
                "spring.datasource.url": "jdbc:postgresql://127.0.0.1:55440/navik_pipeline_e2e",
                "spring.datasource.username": "pipeline_test",
                "spring.datasource.password": "",
                "spring.datasource.hikari.maximum-pool-size": "12",
                "spring.jpa.hibernate.ddl-auto": "update",
                "spring.jpa.open-in-view": "false",
                "spring.jpa.properties.hibernate.hbm2ddl.halt_on_error": "true",
                "spring.sql.init.mode": "always",
                "spring.sql.init.schema-locations": "classpath:db/growth-pipeline.sql",
                "spring.jwt.secret": base64.b64encode(JWT_KEY).decode(),
                "navik.growth-log.evaluation-mode": mode,
                "navik.growth-log.worker.enabled": "true",
                "navik.growth-log.worker.poll-ms": "1000",
                "navik.growth-log.worker.batch-size": "10",
                "navik.growth-log.worker.block-ms": "2000",
                "navik.growth-log.pipeline.enabled": str(mode == "stream").lower(),
            }
        )
    else:
        props.update(
            {
                "spring.ai.openai.api-key": "fixture-only",
                "spring.ai.openai.base-url": "http://127.0.0.1:58083",
                "spring.ai.openai.chat.options.model": "fixture",
                "spring.ai.openai.embedding.options.model": "text-embedding-3-small",
                "spring.ai.retry.max-attempts": "1",
                "navik.growth-pipeline.enabled": str(mode == "stream").lower(),
                "navik.growth-pipeline.jdbc-url": "jdbc:postgresql://127.0.0.1:55440/navik_pipeline_e2e",
                "navik.growth-pipeline.username": "pipeline_test",
                "navik.growth-pipeline.password": "",
                "navik.growth-pipeline.analysis-concurrency": str(concurrency),
                "navik.growth-pipeline.embedding-concurrency": str(concurrency),
                "navik.growth-pipeline.starts-per-minute": "10000",
                "navik.growth-pipeline.max-attempts": "3",
                "navik.growth-pipeline.max-runtime-seconds": "300",
            }
        )
    cp = (root / "build/pipeline-harness-classpath.txt").read_text().replace("\\", "/")
    args = ["-Xms128m", "-Xmx384m", "-XX:ActiveProcessorCount=2", "-cp", cp, main] + [
        "--" + k + "=" + v for k, v in props.items()
    ]
    argfile = OUTPUT / (name + ".args")
    argfile.write_text(
        "\n".join('"' + a.replace("\\", "/").replace('"', '\\"') + '"' for a in args), encoding="utf-8"
    )
    env = os.environ.copy()
    env["PIPELINE_E2E_ENABLED"] = "true"
    # .env auto-discovery is disabled; all integration addresses are fixed loopback fixture addresses.
    env["DOTENV_ENABLED"] = "false"
    log = (OUTPUT / (name + "-" + str(time.time_ns()) + ".log")).open("wb")
    kwargs = {"creationflags": subprocess.CREATE_NO_WINDOW} if os.name == "nt" else {}
    proc = subprocess.Popen(
        ["java", "@" + str(argfile)], cwd=root, env=env, stdout=log, stderr=subprocess.STDOUT, **kwargs
    )
    PROCS[name] = proc
    port = 58081 if name == "spring" else 58082
    until = time.time() + 100
    while time.time() < until:
        if proc.poll() is not None:
            raise RuntimeError(name + " exited; inspect " + log.name)
        try:
            with socket.create_connection(("127.0.0.1", port), timeout=0.3):
                break
        except OSError:
            time.sleep(0.3)
    else:
        raise TimeoutError(name + " did not start")
    emit("process_started", name=name, pid=proc.pid, mode=mode, concurrency=concurrency, log=log.name)


def reset():
    validate_isolation()
    stop("spring")
    stop("crawler")
    REDIS.flushdb()
    sql("DROP SCHEMA public CASCADE; CREATE SCHEMA public; CREATE EXTENSION vector")


def seed():
    sql("INSERT INTO jobs(id,name) VALUES (4,'E2E Backend')")
    sql(
        "INSERT INTO kpi_cards(id,job_id,name,strong_title,strong_content,weak_title,weak_content,image_url) VALUES (1,4,'E2E KPI','s','s','w','w','fixture')"
    )
    for user in range(1, 5):
        sql(
            "INSERT INTO users(id,name,nickname,email,level_value,role,job_id,social_id,social_type,user_status,is_entry_level) VALUES (%s,'fixture',%s,%s,1,'USER',4,%s,'fixture','ACTIVE',true)",
            (user, "fixture" + str(user), str(user) + "@example.test", str(user)),
        )
        sql("INSERT INTO kpi_scores(kpi_card_id,user_id,score) VALUES (1,%s,0)", (user,))


def prepare(mode="stream", concurrency=2):
    reset()
    PROVIDER.reset()
    start("spring", mode)
    seed()
    start("crawler", mode, concurrency)


def submit(marker, user=1):
    started = time.time()
    status, data = http("/v1/growth-logs", {"content": marker}, user=user)
    if status != 201:
        raise AssertionError(("submit", status, data))
    payload = data.get("result", data.get("data", {}))
    job = payload.get("growthLogId", payload.get("id"))
    if job is None:
        raise AssertionError(data)
    return dict(id=job, marker=marker, user=user, submitted=started, accepted=time.time())


def wait_done(jobs, timeout=90, require_all=True):
    end = time.time() + timeout
    last = 0
    while time.time() < end:
        rows = sql(
            "SELECT id,status,total_delta FROM growth_logs WHERE id=ANY(%s)", ([j["id"] for j in jobs],)
        )
        states = {r[0]: r[1] for r in rows}
        for job in jobs:
            if states.get(job["id"]) in ("COMPLETED", "FAILED") and "observed" not in job:
                job.update(observed=time.time(), status=states[job["id"]])
        if all("observed" in j for j in jobs):
            break
        if time.time() - last > 25:
            emit("waiting", states=states)
            last = time.time()
        time.sleep(0.05)
    for job in jobs:
        if "observed" not in job:
            job.update(observed=time.time(), status="TIMEOUT")
    if require_all and any(j["status"] != "COMPLETED" for j in jobs):
        raise AssertionError(jobs)
    return jobs


def assert_effects(jobs):
    for user in set(j["user"] for j in jobs):
        userjobs = [j for j in jobs if j["user"] == user and j["status"] == "COMPLETED"]
        assert sql("SELECT score FROM kpi_scores WHERE user_id=%s", (user,), True) == 3 * len(userjobs)
        assert sql("SELECT count(*) FROM abilities WHERE user_id=%s", (user,), True) == 2 * len(userjobs)
        assert sql(
            "SELECT count(*) FROM ability_embeddings e JOIN abilities a ON a.id=e.ability_id WHERE a.user_id=%s AND vector_dims(e.embedding)=1536",
            (user,),
            True,
        ) == 2 * len(userjobs)
    assert sql("SELECT count(*) FROM growth_log_kpi_links", one=True) == len(
        [j for j in jobs if j["status"] == "COMPLETED"]
    )


def smoke():
    prepare()
    jobs = [submit("JOB_SMOKE_" + str(i), user=i + 1) for i in range(3)]
    wait_done(jobs)
    assert_effects(jobs)
    status, detail = http("/v1/growth-logs/" + str(jobs[0]["id"]) + "/analysis-status")
    assert status == 200, (status, detail)
    assert http("/v1/growth-logs/" + str(jobs[0]["id"]) + "/analysis-status", user=2)[0] == 404
    assert http("/v1/growth-logs/" + str(jobs[0]["id"]) + "/analysis-status", user=None)[0] in (401, 403)
    result = dict(jobs=jobs, provider=PROVIDER.snapshot(), status=detail)
    (OUTPUT / "smoke.json").write_text(json.dumps(result, indent=2), encoding="utf-8")
    emit("smoke_passed", jobs=3, score=9, vectors=6)
    return result


def persist(name, value):
    (OUTPUT / (name + ".json")).write_text(json.dumps(value, indent=2, default=str), encoding="utf-8")


def until(predicate, timeout=60, label="condition"):
    end = time.time() + timeout
    while time.time() < end:
        result = predicate()
        if result:
            return result
        time.sleep(0.1)
    raise TimeoutError(label)


def job_id(job):
    return sql("SELECT id FROM growth_analysis_job WHERE growth_log_id=%s", (job["id"],), True)


def calls_for(job):
    return [
        c
        for c in PROVIDER.snapshot()["calls"]
        if c["key"] in (job["marker"], job["marker"] + "_A", job["marker"] + "_B")
    ]


def call_counts(job):
    return dict(Counter(c["kind"] + ":" + c["key"] for c in calls_for(job)))


def docker(*args):
    return subprocess.run(["docker", *args], check=True, capture_output=True, text=True).stdout.strip()


def validate_isolation():
    # A reset is allowed only against the named disposable Docker fixtures on fixed loopback ports.
    for name, port, host_port, image in [
        ("navik-pipeline-e2e-pg", "5432/tcp", "55440", "pgvector/pgvector:pg17"),
        ("navik-pipeline-e2e-redis", "6379/tcp", "56380", "redis:7.4-alpine"),
    ]:
        info = json.loads(docker("inspect", name))[0]
        assert info["Config"]["Image"] == image, "Unexpected fixture image: " + name
        assert info["HostConfig"]["AutoRemove"], "Fixture must be disposable: " + name
        assert info["NetworkSettings"]["Ports"][port] == [{"HostIp": "127.0.0.1", "HostPort": host_port}], (
            "Unexpected fixture binding: " + name
        )
    assert sql("SELECT current_database()", one=True) == "navik_pipeline_e2e"


def faults():
    prepare()
    jobs = []
    results = []
    # Duplicate results must pass through the real business transaction without a second effect.
    job = submit("JOB_DUPLICATE", 1)
    jobs.append(job)
    wait_done([job])
    for _ in range(5):
        REDIS.xadd(
            "{growth-v2}:results",
            {"schemaVersion": "1", "eventId": str(uuid.uuid4()), "jobId": job_id(job), "stage": "APPLY"},
        )
    until(lambda: REDIS.xlen("{growth-v2}:results") == 0, label="duplicate result ACK")
    assert_effects(jobs)
    results.append(
        dict(scenario="duplicate_results", extra_deliveries=5, kpi_score=3, vectors=2, outcome="PASS")
    )
    emit("fault_passed", **results[-1])

    PROVIDER.failure = "JOB_PARTIAL_B"
    job = submit("JOB_PARTIAL", 2)
    jobs.append(job)
    wait_done([job], timeout=100)
    counts = call_counts(job)
    assert counts == {
        "chat_tool:JOB_PARTIAL": 1,
        "chat_final:JOB_PARTIAL": 1,
        "embedding:JOB_PARTIAL_A": 1,
        "embedding:JOB_PARTIAL_B": 2,
    }, counts
    assert_effects(jobs)
    results.append(dict(scenario="second_embedding_429", calls=counts, outcome="PASS"))
    emit("fault_passed", **results[-1])
    PROVIDER.failure = None

    PROVIDER.slow = "JOB_KILL_B"
    job = submit("JOB_KILL", 3)
    jobs.append(job)
    until(
        lambda: any(
            c["kind"] == "embedding" and c["key"].endswith("JOB_KILL_B") and "started" in c
            for c in calls_for(job)
        ),
        label="second embedding started",
    )
    checkpoint = sql("SELECT result_json FROM growth_analysis_job WHERE id=%s", (job_id(job),), True)
    assert checkpoint and "JOB_KILL_A" in checkpoint, checkpoint
    killed = time.time()
    stop("crawler", force=True)
    start("crawler")
    wait_done([job], timeout=180)
    assert_effects(jobs)
    counts = call_counts(job)
    assert counts == {
        "chat_tool:JOB_KILL": 1,
        "chat_final:JOB_KILL": 1,
        "embedding:JOB_KILL_A": 1,
        "embedding:JOB_KILL_B": 2,
    }, counts
    results.append(
        dict(
            scenario="worker_process_kill",
            recovery_seconds=job["observed"] - killed,
            calls=counts,
            outcome="PASS",
        )
    )
    emit("fault_passed", **results[-1])
    PROVIDER.slow = None

    docker("pause", "navik-pipeline-e2e-redis")
    try:
        job = submit("JOB_REDIS_OUTAGE", 4)
        jobs.append(job)
        durable = sql(
            "SELECT count(*) FROM growth_analysis_outbox WHERE job_id=%s AND published_at IS NULL",
            (job_id(job),),
            True,
        )
        assert durable >= 1, durable
        time.sleep(5)
        assert sql("SELECT status FROM growth_logs WHERE id=%s", (job["id"],), True) == "PENDING"
    finally:
        docker("unpause", "navik-pipeline-e2e-redis")
    wait_done([job], timeout=100)
    assert_effects(jobs)
    results.append(
        dict(
            scenario="redis_paused_during_admission",
            pause_seconds=5,
            unpublished_outbox=durable,
            admission_ms=1000 * (job["accepted"] - job["submitted"]),
            outcome="PASS",
        )
    )
    emit("fault_passed", **results[-1])

    before = sql("SELECT score FROM kpi_scores WHERE user_id=4", one=True)
    vectors = sql("SELECT count(*) FROM ability_embeddings", one=True)
    sql("ALTER TABLE ability_embeddings ADD CONSTRAINT e2e_reject_embedding CHECK (false) NOT VALID")
    try:
        job = submit("JOB_DB_ROLLBACK", 4)
        jobs.append(job)
        until(
            lambda: sql(
                "SELECT error_code='APPLY_RETRY' FROM growth_analysis_job WHERE id=%s", (job_id(job),), True
            ),
            timeout=60,
            label="APPLY transaction rollback",
        )
        assert sql("SELECT score FROM kpi_scores WHERE user_id=4", one=True) == before
        assert sql("SELECT count(*) FROM ability_embeddings", one=True) == vectors
        assert sql("SELECT status FROM growth_logs WHERE id=%s", (job["id"],), True) == "PENDING"
    finally:
        sql("ALTER TABLE ability_embeddings DROP CONSTRAINT e2e_reject_embedding")
    wait_done([job], timeout=90)
    assert_effects(jobs)
    results.append(
        dict(
            scenario="business_embedding_constraint_failure",
            partial_effects=0,
            final_increment=3,
            final_vectors=2,
            outcome="PASS",
        )
    )
    emit("fault_passed", **results[-1])
    report = dict(results=results, jobs=jobs, provider=PROVIDER.snapshot())
    persist("faults", report)
    return report


def percentile(values, p):
    values = sorted(values)
    index = (len(values) - 1) * p / 100
    lower = int(index)
    upper = min(lower + 1, len(values) - 1)
    return values[lower] + (values[upper] - values[lower]) * (index - lower)


def benchmark_case(label, repeat, count, interval, all_jobs):
    jobs = []
    submitting = threading.Event()
    submitting.set()
    errors = []

    # Observe completion while requests are still being submitted; otherwise steady-load E2E is inflated.
    def observe():
        while submitting.is_set() or any("observed" not in j for j in jobs):
            current = list(jobs)
            if current:
                try:
                    rows = sql(
                        "SELECT id,status FROM growth_logs WHERE id=ANY(%s)", ([j["id"] for j in current],)
                    )
                    states = dict(rows)
                    now = time.time()
                    for job in current:
                        if "observed" not in job and states.get(job["id"]) in ("COMPLETED", "FAILED"):
                            job.update(observed=now, status=states[job["id"]])
                except Exception as error:
                    errors.append(str(error))
                    return
            time.sleep(0.05)

    observer = threading.Thread(target=observe, daemon=True)
    observer.start()
    start_at = time.time()
    try:
        for index in range(count):
            if interval:
                time.sleep(max(0, start_at + index * interval - time.time()))
            job = submit("JOB_" + label + "_" + str(repeat) + "_" + str(index), 1 + index % 4)
            jobs.append(job)
    finally:
        submitting.clear()
    observer.join(timeout=120)
    if observer.is_alive() or errors:
        raise AssertionError(("observation", errors))
    assert all(j["status"] == "COMPLETED" for j in jobs), jobs
    all_jobs.extend(jobs)
    assert_effects(all_jobs)
    for job in jobs:
        calls = calls_for(job)
        job["queue_wait_ms"] = 1000 * (min(c["received"] for c in calls) - job["accepted"])
        job["admission_ms"] = 1000 * (job["accepted"] - job["submitted"])
        job["e2e_ms"] = 1000 * (job["observed"] - job["submitted"])
    provider_calls = [c for j in jobs for c in calls_for(j)]
    result = dict(
        label=label,
        repeat=repeat,
        jobs=jobs,
        count=count,
        interval_seconds=interval,
        provider_calls=provider_calls,
        throughput_jobs_per_second=count
        / (max(j["observed"] for j in jobs) - min(j["submitted"] for j in jobs)),
        provider_http_calls=dict(Counter(c["kind"] for c in provider_calls)),
        metrics={
            k: {"p50": percentile([j[k] for j in jobs], 50), "p95": percentile([j[k] for j in jobs], 95)}
            for k in ["admission_ms", "queue_wait_ms", "e2e_ms"]
        },
    )
    persist("bench-" + label + "-" + str(repeat), result)
    emit("benchmark_case_passed", **{k: v for k, v in result.items() if k not in ("jobs", "provider_calls")})
    return result


def benchmark():
    results = []
    for mode, concurrency in [("async", 1), ("stream", 1), ("stream", 2)]:
        prepare(mode, concurrency)
        all_jobs = []
        warmup = [submit("JOB_WARMUP_" + mode + "_" + str(i), i + 1) for i in range(3)]
        wait_done(warmup)
        all_jobs.extend(warmup)
        assert_effects(all_jobs)
        label = mode + "_c" + str(concurrency)
        for repeat in range(1, 4):
            workloads = [("steady", 12, 1.0), ("burst", 24, 0)]
            if repeat % 2 == 0:
                workloads.reverse()
            for shape, count, interval in workloads:
                results.append(benchmark_case(label + "_" + shape, repeat, count, interval, all_jobs))
    report = dict(
        environment={
            "java_heap": "128m..384m",
            "active_processor_count": 2,
            "provider_concurrent_http_limit": 2,
            "chat_http_delay_ms": 200,
            "embedding_http_delay_ms": 100,
            "logical_analysis_chat_http_calls": 2,
            "stream_starts_per_minute_override": 10000,
            "completion_poll_seconds": 0.05,
            "baseline": "same checkout existing async HTTP path",
            "repeats": 3,
            "note": "local synthetic provider; no quality, actual cost or production throughput claim",
        },
        results=results,
    )
    persist("benchmark", report)
    return report


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--phase", choices=["smoke", "faults", "benchmark"], default="smoke")
    parser.add_argument("--out", type=Path)
    args = parser.parse_args()
    if args.out:
        OUTPUT = args.out.resolve()
        OUTPUT.mkdir(parents=True, exist_ok=True)
    server = ThreadingHTTPServer(("127.0.0.1", 58083), MockOpenAI)
    threading.Thread(target=server.serve_forever, daemon=True).start()
    try:
        globals()[args.phase]()
    finally:
        for name in list(PROCS):
            stop(name)
        server.shutdown()
