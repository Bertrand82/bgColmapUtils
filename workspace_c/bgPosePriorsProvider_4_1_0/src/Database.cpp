#include "Database.h"

#include <stdexcept>
#include <utility>

Database::Database(const std::string& db_path) {
  if (sqlite3_open(db_path.c_str(), &db_) != SQLITE_OK) {
    const std::string err = db_ ? sqlite3_errmsg(db_) : "unknown sqlite open error";
    if (db_ != nullptr) {
      sqlite3_close(db_);
      db_ = nullptr;
    }
    throw std::runtime_error("Cannot open database: " + err);
  }
}

Database::~Database() {
  if (db_ != nullptr) {
    sqlite3_close(db_);
    db_ = nullptr;
  }
}

Database::Database(Database&& other) noexcept : db_(other.db_) {
  other.db_ = nullptr;
}

Database& Database::operator=(Database&& other) noexcept {
  if (this != &other) {
    if (db_ != nullptr) {
      sqlite3_close(db_);
    }
    db_ = other.db_;
    other.db_ = nullptr;
  }
  return *this;
}

sqlite3* Database::Handle() const { return db_; }
