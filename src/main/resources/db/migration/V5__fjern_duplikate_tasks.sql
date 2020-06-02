DELETE
FROM task
WHERE status != 'FERDIG'
  AND exists(SELECT s.id
             FROM soknad s
             WHERE s.id = task.payload
               AND s.saksnummer IS NOT NULL)
