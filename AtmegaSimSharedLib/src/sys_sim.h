#ifndef SYS_SIM_H
#define SYS_SIM_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include <unistd.h>  

typedef uint8_t Sys_Event;
  
struct Sys
{
  Sys_Event  eventFlag;
  uint8_t    taskErr_u8;
};

extern volatile struct Sys sys;

Sys_Event  sys_setEvent             (Sys_Event event);
Sys_Event  sys_clearEvent           (Sys_Event event);
Sys_Event  sys_isEventPending       (Sys_Event event);    
void       sys_inc8BitCnt           (volatile uint8_t *count);
void       sys_inc16BitCnt          (volatile uint16_t *count);
void       sys_500us_isr            ();

extern void sys_printf (const char *format, ...);
extern void sys_log (const char *fileName, int line, __pid_t pid, const char *format, ...);
extern __pid_t sys_pid ();



#define ISR(x) void x ()
#define cli(...) // cli()
#define sei(...) // sei()

#ifdef __cplusplus
}
#endif

#endif /* SYS_SIM_H */

