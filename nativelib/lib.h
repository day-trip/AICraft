#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>


typedef struct State {
  int64_t x;
  int64_t y;
  int64_t z;
  double k0;
  double k1;
} State;

void init(void);

void pf_init(struct State start, struct State goal);

void pf_update_cell(struct State cell, double cost);

int16_t pf_replan(void);

void pf_update_start(struct State start);

void pf_update_goal(struct State goal);

uintptr_t pf_get_path_len(void);

void pf_get_path(struct State *arr);

uintptr_t pf_get_debug_len(void);

void pf_get_debug(struct State *arr);

void chunk_build(int64_t x, int64_t y, const int8_t *arr, int32_t len);

void chunk_remove(int64_t x, int64_t y);

void chunk_set(int64_t x, int64_t y, int64_t z, int8_t value);
