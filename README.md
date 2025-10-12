# Para Testar

1. Instalar ollama: https://ollama.com/download
2. Baixar o modelo que está sendo utilizado `ollama pull llama3.2`
3. Rodar o modelo utilizando `ollama serve`
4. Executar **NotafiscalApplication.java**
5. Chamada Rest para o endpoit
- Via browser: http://localhost:8080/notafiscal/ler
- Via cmd: curl -X GET http://localhost:8080/notafiscal/ler