package ui;

import java.util.Scanner;

// centraliza a leitura do teclado
// lê sempre linha inteira(nextLine), evitando problema de quebra de
// linha que sobra no buffer que sobra depois do nextInt()

public class Console {

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
}