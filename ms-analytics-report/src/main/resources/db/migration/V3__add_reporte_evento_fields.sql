ALTER TABLE reporte ADD COLUMN evento_id BIGINT;
ALTER TABLE reporte ADD COLUMN total_tickets INT DEFAULT 0;
ALTER TABLE reporte ADD COLUMN total_inscripciones INT DEFAULT 0;
