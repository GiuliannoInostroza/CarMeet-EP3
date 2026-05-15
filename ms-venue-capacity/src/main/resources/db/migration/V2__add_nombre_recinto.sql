ALTER TABLE recinto ADD COLUMN nombre VARCHAR(255);
ALTER TABLE recinto MODIFY COLUMN capacidad_maxima INT NULL;
ALTER TABLE recinto MODIFY COLUMN ocupacion_actual INT NULL;
