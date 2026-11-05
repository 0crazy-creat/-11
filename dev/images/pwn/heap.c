// Heap Lab: 8 个 32 字节 chunk，可 alloc/free/edit/show。
// free 之后指针未清空（Use-After-Free），edit 可覆写已释放 chunk 的 tcache fd。
// -no-pie 编译，win() 地址固定，可配合泄露的 chunk 地址完成 tcache poisoning。
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static char *chunks[8];

__attribute__((used)) static void win(void) {
    system("cat /flag.txt 2>/dev/null || echo 'flag not mounted'");
    _exit(0);
}

static void menu(void) {
    puts("1. alloc  2. free  3. edit  4. show  5. quit");
}

int main(void) {
    setvbuf(stdout, NULL, _IONBF, 0);
    puts("=== Heap Lab ===");
    menu();

    int op, idx;
    while (scanf("%d", &op) == 1) {
        if (op == 1) {
            if (scanf("%d", &idx) != 1) break;
            if (idx < 0 || idx > 7) continue;
            chunks[idx] = malloc(32);
            printf("chunk[%d] = %p\n", idx, (void *) chunks[idx]);
        } else if (op == 2) {
            if (scanf("%d", &idx) != 1) break;
            if (idx >= 0 && idx < 8 && chunks[idx]) {
                free(chunks[idx]);
            }
        } else if (op == 3) {
            if (scanf("%d", &idx) != 1) break;
            if (idx >= 0 && idx < 8 && chunks[idx]) {
                read(0, chunks[idx], 48);
            }
        } else if (op == 4) {
            if (scanf("%d", &idx) != 1) break;
            if (idx >= 0 && idx < 8 && chunks[idx]) {
                printf("%s\n", chunks[idx]);
            }
        } else {
            break;
        }
        menu();
    }
    return 0;
}
