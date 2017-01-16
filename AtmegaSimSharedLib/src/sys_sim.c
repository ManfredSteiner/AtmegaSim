#include "sys_sim.h"
#include "app.h"
#include <unistd.h>
#include <time.h>

volatile struct Sys sys;

__pid_t sys_pid ()
{
  return getpid(); 
}


Sys_Event sys_setEvent (Sys_Event event)
{
  uint8_t eventIsPending = 0;
  
  if (sys.eventFlag & event)
    eventIsPending = 1;
  sys.eventFlag |= event;
  return eventIsPending;
}


Sys_Event sys_clearEvent (Sys_Event event)
{
  uint8_t eventIsPending = 0;

  if (sys.eventFlag & event)
    eventIsPending = 1;
  sys.eventFlag &= ~event;

  return eventIsPending;  
}


Sys_Event sys_isEventPending (Sys_Event event)
{
  return 0;  
}


void sys_inc8BitCnt (volatile uint8_t *count)
{
  *count = *count < 0xff ? *count + 1 : *count;
}


void sys_inc16BitCnt (volatile uint16_t *count)
{
  *count = *count < 0xffff ? *count + 1 : *count;
}


void sys_500us_isr ()
{
  static uint8_t cnt500us = 0;
  struct timespec timeStart, timeEnd; // time_t tv_sec; long tv_nsec;
           
  if (clock_gettime(CLOCK_MONOTONIC, &timeStart) != 0)
    timeStart.tv_nsec = -1;

  cnt500us++;
  
  if      (cnt500us & 0x01) app_task_1ms();
  else if (cnt500us & 0x02) app_task_2ms();
  else if (cnt500us & 0x04) app_task_4ms();
  else if (cnt500us & 0x08) app_task_8ms();
  else if (cnt500us & 0x10) app_task_16ms();
  else if (cnt500us & 0x20) app_task_32ms();
  else if (cnt500us & 0x40) app_task_64ms();
  else if (cnt500us & 0x80) app_task_128ms();
  
  if (timeStart.tv_nsec >= 0)
  {
    if (clock_gettime(CLOCK_MONOTONIC, &timeEnd) == 0) 
    {
      long dt = 0;
      if (timeStart.tv_sec != timeEnd.tv_sec)
        dt = (timeEnd.tv_sec - timeStart.tv_sec)*1000000000L;
      dt = dt + timeEnd.tv_nsec - timeStart.tv_nsec;
      //sys_log(__FILE__, __LINE__, getpid(), "dt = %ld us", dt/1000);
      if (dt > 500000)
        sys_inc8BitCnt(&sys.taskErr_u8);
    }
  }
  
}