package com.hackaton.notafiscal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/notafiscal")
public class processamentoNotaFiscal {
    
    @Value("${pasta.notafiscal}")
    private String caminhoPasta;

    @Value("${ollama.api.url}")
    private String apiUrl;
    
    @Value("${ollama.model}")
    private String modelo;

    @GetMapping("/ler")
    public String lerNotaFiscal() throws Exception {
        File pasta = new File(caminhoPasta);
        List<String> notas = new ArrayList<>();
        
        if (!pasta.exists() || !pasta.isDirectory()) {
            System.out.println("Pasta não encontrada: " + caminhoPasta);
        }
        
        File[] arquivos = pasta.listFiles((_, nome) -> nome.toLowerCase().endsWith(".pdf"));
        
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                try (PDDocument document = Loader.loadPDF(arquivo)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String texto = stripper.getText(document);
                    notas.add(texto);
                }
            }
        }

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> respostas = new ArrayList<>();

        for (String nota : notas) {
            Map<String, Object> request = new HashMap<>();
            request.put("model", modelo);
            request.put("prompt", """
                Extraia todas as informações possível dessa nota fiscal eletronia e gere um JSON com todos os campos:
                {
                    especie: null,
                    tipoRecebimento: null,
                    data: null,
                    fornecedor: null,
                    notaFiscal: null,
                    serie: null,
                    dataEmissao: null,
                    naturezaOperacao: null,
                    vencimento: null,
                    valorTotal: null,
                    tipoFrete: null,
                    contaDebito: null,
                    contaCredito: null
                }
            """ + "\n\nDocument :\n" + nota);
            request.put("stream", false);
            
            String retorno = restTemplate.postForObject(apiUrl, request, String.class);
            
            String resposta = objectMapper.readTree(retorno).get("response").asText();
            
            respostas.add(resposta);
        }

        return String.join("\n\n\n", respostas);
    }
}
