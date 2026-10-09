#!/bin/bash
set -e
mkdir -p out
for f in build/libs/*.jar; do
  case "$f" in
    *-sources.jar|*-dev.jar) ;;
    *) cp "$f" out/TotemAstraeusv2.jar ;;
  esac
done
