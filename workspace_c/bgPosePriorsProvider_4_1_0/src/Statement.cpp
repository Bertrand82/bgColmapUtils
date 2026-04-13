#include "Statement.h"

#include "Database.h"

#include <stdexcept>
#include <utility>

Statement::Statement(const Database& database, const std::string& sql) {
  const int rc = sqlite3_prepare_v2(database.Handle(), sql.c_str(), -1, &stmt_, nullptr);
  if (rc != SQLITE_OK) {
    throw std::runtime_error("Failed to prepare statement: " + std::string(sqlite3_errstr(rc)));
  }
}

Statement::~Statement() {
  if (stmt_ != nullptr) {
    sqlite3_finalize(stmt_);
    stmt_ = nullptr;
  }
}

Statement::Statement(Statement&& other) noexcept : stmt_(other.stmt_) {
  other.stmt_ = nullptr;
}

Statement& Statement::operator=(Statement&& other) noexcept {
  if (this != &other) {
    if (stmt_ != nullptr) {
      sqlite3_finalize(stmt_);
    }
    stmt_ = other.stmt_;
    other.stmt_ = nullptr;
  }
  return *this;
}

bool Statement::Step() {
  const int rc = sqlite3_step(stmt_);
  if (rc == SQLITE_ROW) {
    return true;
  }
  if (rc == SQLITE_DONE) {
    return false;
  }
  throw std::runtime_error("Statement execution failed: " + std::string(sqlite3_errstr(rc)));
}

void Statement::Reset() {
  const int rc = sqlite3_reset(stmt_);
  if (rc != SQLITE_OK) {
    throw std::runtime_error("Failed to reset statement: " + std::string(sqlite3_errstr(rc)));
  }
}

void Statement::ClearBindings() {
  const int rc = sqlite3_clear_bindings(stmt_);
  if (rc != SQLITE_OK) {
    throw std::runtime_error("Failed to clear statement bindings: " +
                             std::string(sqlite3_errstr(rc)));
  }
}

void Statement::BindText(int index, const std::string& value) {
  const int rc = sqlite3_bind_text(stmt_, index, value.c_str(), -1, SQLITE_TRANSIENT);
  if (rc != SQLITE_OK) {
    throw std::runtime_error("Failed to bind text parameter: " + std::string(sqlite3_errstr(rc)));
  }
}

void Statement::BindInt64(int index, std::int64_t value) {
  const int rc = sqlite3_bind_int64(stmt_, index, value);
  if (rc != SQLITE_OK) {
    throw std::runtime_error("Failed to bind int64 parameter: " + std::string(sqlite3_errstr(rc)));
  }
}

void Statement::BindBlob(int index, const std::vector<double>& values) {
  const void* data_ptr = values.empty() ? nullptr : static_cast<const void*>(values.data());
  const int byte_size = static_cast<int>(values.size() * sizeof(double));
  const int rc = sqlite3_bind_blob(stmt_, index, data_ptr, byte_size, SQLITE_TRANSIENT);
  if (rc != SQLITE_OK) {
    throw std::runtime_error("Failed to bind blob parameter: " + std::string(sqlite3_errstr(rc)));
  }
}

int Statement::ColumnCount() const { return sqlite3_column_count(stmt_); }

long long Statement::ColumnInt64(int index) const { return sqlite3_column_int64(stmt_, index); }

const char* Statement::ColumnName(int index) const {
  return sqlite3_column_name(stmt_, index);
}

const char* Statement::ColumnText(int index) const {
  const auto* txt = sqlite3_column_text(stmt_, index);
  return txt == nullptr ? "NULL" : reinterpret_cast<const char*>(txt);
}
