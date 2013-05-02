#!/bin/bash
latest_ver=`git branch -r | grep -o 'ohmage-[0-9].[0-9]*' | awk -F. '{print $2}' | sort -nr | head -1`
branch=`git branch -r | grep -o 'ohmage-[0-9].'$latest_ver`
echo $branch
