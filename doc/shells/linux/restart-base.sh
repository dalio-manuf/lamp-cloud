#!/bin/bash
PROFILE=${1:-prod}
sh run.sh restart lamp-base-server "$PROFILE"
