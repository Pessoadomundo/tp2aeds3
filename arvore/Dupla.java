package arvore;

/**
 * Classe que representa um par contendo a chave do registro e a sua posição no
 * arquivo.
 */
public class Dupla {
    int chave;
    long posicao;

    public Dupla(int chave, long posicao) {
        this.chave = chave;
        this.posicao = posicao;
    }

    /**
     * Método que ordena um vetor de duplas tomando como base o atributo chave.
     * 
     * @param arr
     */
    public static void sort(Dupla[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i].chave > arr[j].chave) {
                    Dupla aux = arr[i];
                    arr[i] = arr[j];
                    arr[j] = aux;
                }
            }
        }
    }
}