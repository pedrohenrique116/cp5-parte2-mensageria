#!/bin/sh
# Mesmo comando da aula: sobe o RabbitMQ (e tambem o PostgreSQL e o Mailpit)
docker compose up -d
echo "RabbitMQ:  http://localhost:15672  (guest / guest)"
echo "Mailpit:   http://localhost:8025"
echo "Postgres:  localhost:5432  banco mercado_express (postgres / postgres)"
