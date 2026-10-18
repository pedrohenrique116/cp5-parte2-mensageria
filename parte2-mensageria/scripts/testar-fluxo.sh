#!/bin/sh
# Script de testes via curl (Git Bash, Linux ou macOS).
# Cria 5 compras, espera o Banco Tranquilo responder e mostra o resultado.
BASE=${BASE:-http://localhost:8081}

echo "== Produtos =="
curl -s "$BASE/api/produtos"; echo

for EMAIL in cliente1@exemplo.com cliente2@exemplo.com joao.aprova@exemplo.com ana.recusa@exemplo.com cliente3@exemplo.com; do
  echo "== Comprando para $EMAIL =="
  curl -s -X POST "$BASE/api/compras" -H "Content-Type: application/json" \
    -d "{\"clienteNome\":\"Teste Script\",\"clienteEmail\":\"$EMAIL\",\"itens\":[{\"produtoId\":1,\"quantidade\":1},{\"produtoId\":10,\"quantidade\":2}]}"
  echo
done

echo "Aguardando o Banco Tranquilo (5 s)..."
sleep 5

echo "== Compras =="
curl -s "$BASE/api/compras"; echo
echo "== E-mails registrados =="
curl -s "$BASE/api/emails"; echo
echo "== Dashboard =="
curl -s "$BASE/api/dashboard/resumo"; echo
