/*
 * Copyright 2017 "Technology" LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

WITH blocks AS (
    SELECT 
        blocking_locks.pid as pid, 
        blocked_locks.pid as blocked_pid,
        blocking_locks.locktype as locktype,
        blocking_locks.relation::regclass as relation
    FROM 
        pg_catalog.pg_locks blocked_locks
    JOIN 
        pg_catalog.pg_locks blocking_locks 
            ON blocking_locks.locktype = blocked_locks.locktype
        AND blocking_locks.DATABASE IS NOT DISTINCT FROM blocked_locks.DATABASE
        AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation
        AND blocking_locks.page IS NOT DISTINCT FROM blocked_locks.page
        AND blocking_locks.tuple IS NOT DISTINCT FROM blocked_locks.tuple
        AND blocking_locks.virtualxid IS NOT DISTINCT FROM blocked_locks.virtualxid
        AND blocking_locks.transactionid IS NOT DISTINCT FROM blocked_locks.transactionid
        AND blocking_locks.classid IS NOT DISTINCT FROM blocked_locks.classid
        AND blocking_locks.objid IS NOT DISTINCT FROM blocked_locks.objid
        AND blocking_locks.objsubid IS NOT DISTINCT FROM blocked_locks.objsubid
        AND blocking_locks.pid != blocked_locks.pid
    WHERE NOT blocked_locks.granted
)
SELECT 
    procs.pid AS pid, 
    application_name, 
    datname, 
    usename,
    CASE WHEN client_port=-1 THEN 'local pipe' 
         WHEN length(client_hostname)>0 THEN client_hostname||':'||client_port 
         ELSE textin(inet_out(client_addr))||':'||client_port 
    END AS client, 
    date_trunc('second', backend_start) AS backend_start, 
    CASE WHEN state='active' THEN date_trunc('second', query_start)::text 
         ELSE '' 
    END AS query_start, 
    date_trunc('second', xact_start) AS xact_start, 
    state, 
    date_trunc('second', state_change) AS state_change, 
    blocks.pid AS blockedby, 
    /* deprecated
    null::text AS blocking_locks,*/ 
    blocks.locktype AS locktype, 
    blocks.relation AS relation, 
    query AS query, 
    CASE WHEN query_start IS NULL OR state<>'active' THEN false 
         ELSE query_start < now() - '10 seconds'::interval 
    END AS slowquery 
FROM 
    pg_stat_activity procs 
    LEFT JOIN blocks 
        ON blocks.blocked_pid = procs.pid 
ORDER BY 
    pid
