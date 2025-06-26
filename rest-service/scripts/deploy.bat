@echo off

cd /d C:\Users\user2\IdeaProjects\Practika

set ENV_FILE=./.env

git checkout dev
git pull origin dev

docker compose -f docker-compose.yml --env-file %ENV_FILE% down --timeout=60 --remove-orphans

docker compose -f docker-compose.yml --env-file %ENV_FILE% up --build --detach

echo Deploy complete
pause
