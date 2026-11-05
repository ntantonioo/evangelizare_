package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

// centraliza a leitura do teclado
// lê sempre linha inteira(nextLine), evitando problema de quebra de
// linha que sobra no buffer que sobra depois do nextInt()

public class Console {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.STRICT);

    private final Scanner scanner = new Scanner(System.in);

    public String lerTexto() {
        return scanner.nextLine();
    }

    public int lerInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Digite um número válido: ");
            }
        }
    }

    // Lê uma data no formato dd/mm/aaaa. Linha vazia devolve null
    public LocalDate lerDataOpcional() {
        while (true) {
            String texto = scanner.nextLine().trim();

            if (texto.isEmpty()) {
                return null;
            }

            try {
                return converterData(texto);
            } catch (DateTimeParseException e) {
                System.out.print("Data inválida. Use dd/mm/aaaa ou deixe em branco: ");
            }
        }
    }

    public static LocalDate converterData(String texto) throws DateTimeParseException {
        return LocalDate.parse(texto.trim(), FORMATO_DATA);
    }

    public static String formatarData(LocalDate data) {
        return data.format(FORMATO_DATA);
    }
}