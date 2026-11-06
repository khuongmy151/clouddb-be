package com.example.clouddbbe.module.activity.entity;

public enum ActionType {
    // User actions
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    BAN_USER,
    UNBAN_USER,
    CHANGE_PASSWORD,
    LOGIN,
    LOGOUT,

    // Database actions
    CREATE_DB,
    UPDATE_DB,
    DELETE_DB,
    START_DB,
    STOP_DB,
    BACKUP_DB,
    RESTORE_DB,

    // Admin actions
    ASSIGN_ROLE
}
