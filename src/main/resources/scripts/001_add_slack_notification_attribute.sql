CREATE TEMPORARY TABLE temp_project AS
    SELECT project_id
    FROM project_attribute
    GROUP BY project_id
    EXCEPT
    SELECT project_id
    FROM project_attribute
    WHERE attribute_id = (SELECT id FROM attribute WHERE name = 'notifications.telegram.enabled');

INSERT INTO attribute (name)
SELECT 'notifications.telegram.enabled'
WHERE NOT EXISTS (SELECT 1 FROM attribute WHERE name = 'notifications.telegram.enabled');

WITH attr AS (
    SELECT id
    FROM attribute
    WHERE name = 'notifications.telegram.enabled'
)
INSERT INTO project_attribute(attribute_id, value, project_id)
SELECT attr.id, 'true', temp.project_id
FROM temp_project temp
CROSS JOIN attr;

DROP TABLE temp_project;