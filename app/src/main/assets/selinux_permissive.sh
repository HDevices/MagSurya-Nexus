#!/system/bin/sh
if [ "$(getenforce)" = "Enforcing" ]; then
  setenforce 0
fi