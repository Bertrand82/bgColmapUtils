#pragma once

#include <sqlite3.h>

#include <cstdint>
#include <string>
#include <vector>

class Database;

class Statement {
 public:
  Statement(const Database& database, const std::string& sql);
  ~Statement();

  Statement(const Statement&) = delete;
  Statement& operator=(const Statement&) = delete;
  Statement(Statement&& other) noexcept;
  Statement& operator=(Statement&& other) noexcept;

  bool Step();
  void Reset();
  void ClearBindings();
  void BindText(int index, const std::string& value);
  void BindInt64(int index, std::int64_t value);
  void BindBlob(int index, const std::vector<double>& values);

  int ColumnCount() const;
  long long ColumnInt64(int index) const;
  const char* ColumnName(int index) const;
  const char* ColumnText(int index) const;

 private:
  sqlite3_stmt* stmt_ = nullptr;
};
