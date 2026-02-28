#include <sqlite3.h>

#include <iostream>
#include <optional>
#include <string>

#include "get_imageId_by_filename.hpp"

std::optional<int> get_imageId_by_filename(const char* dbPath, const char* fileName) {
	sqlite3* db = nullptr;
	if (sqlite3_open(dbPath, &db) != SQLITE_OK) {
		std::cerr << "Cannot open database: " << (db ? sqlite3_errmsg(db) : "unknown") << "\n";
		if (db) sqlite3_close(db);
		return std::nullopt;
	}

	const char* sql = "SELECT image_id FROM images WHERE name = ? LIMIT 1;";

	sqlite3_stmt* stmt = nullptr;
	if (sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) != SQLITE_OK) {
		std::cerr << "SQLite error in sqlite3_prepare_v2: " << sqlite3_errmsg(db) << "\n";
		sqlite3_close(db);
		return std::nullopt;
	}

	if (sqlite3_bind_text(stmt, 1, fileName, -1, SQLITE_TRANSIENT) != SQLITE_OK) {
		std::cerr << "SQLite error in sqlite3_bind_text: " << sqlite3_errmsg(db) << "\n";
		sqlite3_finalize(stmt);
		sqlite3_close(db);
		return std::nullopt;
	}

	int rc = sqlite3_step(stmt);
	if (rc == SQLITE_ROW) {
		int image_id = sqlite3_column_int(stmt, 0);
		sqlite3_finalize(stmt);
		sqlite3_close(db);
		return image_id;
	} else if (rc == SQLITE_DONE) {
		sqlite3_finalize(stmt);
		sqlite3_close(db);
		return std::nullopt;
	} else {
		std::cerr << "SQLite error in sqlite3_step: " << sqlite3_errmsg(db) << "\n";
		sqlite3_finalize(stmt);
		sqlite3_close(db);
		return std::nullopt;
	}

	return std::nullopt;
}
