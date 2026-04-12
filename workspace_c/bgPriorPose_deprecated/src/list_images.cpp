#include <sqlite3.h>

#include <iostream>
#include <string>

static void die(sqlite3* db, const std::string& where) {
    std::cerr << "SQLite error in " << where << ": " << sqlite3_errmsg(db) << "\n";

    if (db) {
        std::cerr << "Tables in database:\n";
        const char* listTablesSql =
            "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;";
        sqlite3_stmt* tablesStmt = nullptr;

        if (sqlite3_prepare_v2(db, listTablesSql, -1, &tablesStmt, nullptr) == SQLITE_OK) {
            bool hasTables = false;
            while (sqlite3_step(tablesStmt) == SQLITE_ROW) {
                hasTables = true;
                const unsigned char* tableName = sqlite3_column_text(tablesStmt, 0);
                std::cerr << " - "
                          << (tableName ? reinterpret_cast<const char*>(tableName) : "")
                          << "\n";
            }
            if (!hasTables) {
                std::cerr << " - <none>\n";
            }
            sqlite3_finalize(tablesStmt);
        } else {
            std::cerr << "Could not list tables: " << sqlite3_errmsg(db) << "\n";
        }

        sqlite3_close(db);
    }

    std::cerr << "Cannot continue, exiting.\n";
    std::exit(1);
}

int list_images(const char* dbPath) {

    sqlite3* db = nullptr;
    if (sqlite3_open(dbPath, &db) != SQLITE_OK) {
        std::cerr << "Cannot open database: " << (db ? sqlite3_errmsg(db) : "unknown") << "\n";
        if (db) sqlite3_close(db);
        return 1;
    }

    const char* sql = "SELECT image_id, name, camera_id FROM images ORDER BY image_id;";

    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
        die(db, "sqlite3_prepare_v2");
    }

    while (true) {
        int rc = sqlite3_step(stmt);
        if (rc == SQLITE_ROW) {
            int image_id = sqlite3_column_int(stmt, 0);
            const unsigned char* name = sqlite3_column_text(stmt, 1);
            int camera_id = sqlite3_column_int(stmt, 2);
            std::cout <<"imageId:" << image_id << " | name:" << (name ? reinterpret_cast<const char*>(name) : "") << "| camera_id : "  << camera_id << "\n";
        } else if (rc == SQLITE_DONE) {
            break;
        } else {
            sqlite3_finalize(stmt);
            die(db, "sqlite3_step");
        }
    }

    sqlite3_finalize(stmt);
    sqlite3_close(db);
    return 0;
}

