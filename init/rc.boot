#!/bin/execlineb -P

/bin/emptyenv
/bin/export PATH /usr/sbin:/usr/bin:/sbin:/bin
/bin/cd /
s6-setsid -qb
umask 022

#if { mount -n -t devtmpfs devtmpfs /dev }
redirfd -r 0 /dev/null
if {
  ifte
  { mount -n -t tmpfs -o remount,rw,mode=0755,size=10M tmpfs /run }
  { mount -n -t tmpfs -o rw,mode=0755,size=10M tmpfs /run }
  mountpoint -q /run
}
if { /etc/slew/init/load_clock.rc }
if { cp -a /etc/slew/run / }
if { mkfifo -m 600 /run/service/s6-svscan.log/fifo }
s6-envdir -I /etc/slew/env

redirfd -wnb 1 /run/service/s6-svscan.log/fifo
background
{
  s6-setsid
  redirfd -w 1 /run/service/s6-svscan.log/fifo
  fdmove -c 2 1
  /etc/slew/init/rc.init
}
unexport !

fdmove -c 2 1
cd /run/service
s6-svscan -st0

