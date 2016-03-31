-- Adds a bit column in campaign table to determine if campaign is editable or not.
-- defaults to false since the "participatory sensing"-ness of it is a bit lacking.
ALTER TABLE campaign
    ADD COLUMN `editable` bit NOT NULL DEFAULT FALSE;