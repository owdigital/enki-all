#!/bin/bash
# Based on https://unix.stackexchange.com/a/82615/73838
function retry()
{
    [[ $# -le 1 ]] && {
        echo "Usage $0 <retry_number> <Command>";
    }
    local n=0
    local try=$1
    local cmd="${@: 2}"

    until [[ $n -ge $try ]]
    do
        $cmd && break || {
            echo
            echo "command '$cmd' failed on try $((n+1)) of $try"
            ((n++))
            if [[ $n -lt $try ]]; then
                echo "Sleeping for 1s before retry"
                echo
                sleep 1;
            else
                echo "Ran out of retries"
                return -1
            fi
        }
    done
}

retry $*