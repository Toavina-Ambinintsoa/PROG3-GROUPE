INSERT INTO collectivity_members (collectivity_id, member_id)
SELECT collectivity_id, id FROM members
WHERE collectivity_id IS NOT NULL;
ALTER TABLE members DROP CONSTRAINT members_collectivity_id_fkey;
ALTER TABLE members DROP COLUMN collectivity_id;