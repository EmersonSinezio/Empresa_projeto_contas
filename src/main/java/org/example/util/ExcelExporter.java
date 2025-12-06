package org.example.util;

import org.example.model.Contas;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public static void exportarParaExcel(List<Contas> contas, String caminhoArquivo) throws IOException {
        // Garante a extensão .xlsx
        if (!caminhoArquivo.toLowerCase().endsWith(".xlsx")) {
            caminhoArquivo += ".xlsx";
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatório de Contas");

            // --- Estilo do Cabeçalho ---
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // --- Estilo de Moeda (R$) ---
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("R$ #,##0.00"));

            // --- Criar Cabeçalho ---
            Row headerRow = sheet.createRow(0);
            String[] colunas = {
                "ID", "Mês Ref", "Fornecedor", "Serviço/Produto", 
                "Valor NF", "Valor Boleto", "Vencimento", 
                "Pagamento/Baixa", "Status", "Centro Custo"
            };

            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- Preencher Dados ---
            int rowNum = 1;
            for (Contas c : contas) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(safe(c.getMesReferencia()));
                row.createCell(2).setCellValue(safe(c.getFornecedor()));
                row.createCell(3).setCellValue(safe(c.getServicoProduto()));

                // Valores Numéricos (Importante para somar no Excel)
                Cell cellNF = row.createCell(4);
                cellNF.setCellValue(c.getValorNF());
                cellNF.setCellStyle(currencyStyle);

                Cell cellBoleto = row.createCell(5);
                cellBoleto.setCellValue(c.getValorBoleto());
                cellBoleto.setCellStyle(currencyStyle);

                row.createCell(6).setCellValue(safe(c.getVencimento()));
                // Data de Pagamento/Baixa (usando Data Lancamento como exemplo de quando foi pago)
                row.createCell(7).setCellValue(safe(c.getDataLancamento())); 
                
                String status = c.isVencida() ? "VENCIDA" : (c.isLancada() ? "PAGA" : "ABERTO");
                row.createCell(8).setCellValue(status);
                
                row.createCell(9).setCellValue(safe(c.getCentroCusto()));
            }

            // Ajustar largura das colunas automaticamente
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escrever o arquivo
            try (FileOutputStream fileOut = new FileOutputStream(caminhoArquivo)) {
                workbook.write(fileOut);
            }
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
