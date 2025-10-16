#!/system/bin/sh
if [ "$(getenforce)" = "Permissive" ]; then
  setenforce 1
fi