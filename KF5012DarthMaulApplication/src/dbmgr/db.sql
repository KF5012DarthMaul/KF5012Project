CREATE TABLE IF NOT EXISTS tblUsers(
    username TEXT PRIMARY KEY,
    hashpass TEXT NOT NULL,
    permission_flags INTEGER
);

CREATE TABLE IF NOT EXISTS tblTasks(
    task_id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_type INTEGER NOT NULL,
    caretaker TEXT,
    execution_day INTEGER,
    FOREIGN KEY(task_type) REFERENCES tblTaskType (type_id) ON DELETE CASCADE,
    FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE,
);

CREATE TABLE IF NOT EXISTS tblTaskType(
    type_id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_name TEXT NOT NULL,
    task_descr TEXT
);

-- Tasks could ref log_id instead of log referencing task_id
-- That would require updating the task row instead of just adding to taskLog.
CREATE TABLE IF NOT EXISTS tblTaskLog(
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    log_time INTEGER NOT NULL,
    completion_time INTEGER,
    FOREIGN KEY(task_id) REFERENCES tblTasks(task_id) ON DELETE CASCADE,
);