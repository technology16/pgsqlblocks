WITH bl AS (
    SELECT blocking_locks.pid as pid, 
    blocked_locks.pid as blocked_pid
    FROM pg_catalog.pg_locks blocked_locks
    JOIN pg_catalog.pg_stat_activity blocked_activity 
        ON blocked_activity.pid = blocked_locks.pid
    JOIN pg_catalog.pg_locks blocking_locks 
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
    JOIN pg_catalog.pg_stat_activity sa ON sa.pid = blocking_locks.pid
    WHERE NOT blocked_locks.granted
)
SELECT 
    /*---------------------*/
    p.pid AS pid, 
    /*---------------------*/
    application_name, 
    /*---------------------*/
    datname, 
    /*---------------------*/
    usename,
    /*---------------------*/
    CASE WHEN client_port=-1 THEN 'local pipe' 
         WHEN length(client_hostname)>0 THEN client_hostname||':'||client_port 
         ELSE textin(inet_out(client_addr))||':'||client_port 
    END AS client, 
    /*---------------------*/
    date_trunc('second', backend_start) AS backend_start, 
    /*---------------------*/
    CASE WHEN state='active' THEN date_trunc('second', query_start)::text 
         ELSE '' 
    END AS query_start, 
    /*---------------------*/
    date_trunc('second', xact_start) AS xact_start, 
    /*---------------------*/
    state, 
    /*---------------------*/
    date_trunc('second', state_change) AS state_change, 
    /*---------------------*/
    (SELECT min(l1.pid) 
        FROM pg_locks l1 WHERE GRANTED AND (relation IN (
                SELECT relation FROM pg_locks l2 WHERE l2.pid=p.pid AND NOT granted
            ) OR transactionid IN (
                SELECT transactionid FROM pg_locks l3 WHERE l3.pid=p.pid AND NOT granted
    ))) AS blockedby,
    /*---------------------*/
    bl.pid as blocking_locks,
    /*---------------------*/
    query AS query, 
    /*---------------------*/
    CASE WHEN query_start IS NULL OR state<>'active' THEN false 
         ELSE query_start < now() - '10 seconds'::interval 
    END AS slowquery 
    /*---------------------*/
    FROM pg_stat_activity p 
    LEFT JOIN bl
    ON bl.blocked_pid = p.pid
    ORDER BY 1 ASC