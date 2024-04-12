package oliv;
import java.util.ArrayList;

/**arquivo criado
 * se nao existir no inicio do arquivo uma raiz, ja cria ela
 * 
 */

class ParIdEndereco {
    int id;
    long enderecoArquivo;

    public ParIdEndereco(int id, long endereco) {
        this.id = id;
        this.enderecoArquivo = endereco;
    }
}

class NoArvoreB {
    ArrayList<ParIdEndereco> pares;
    ArrayList<NoArvoreB> filhos;
    boolean eFolha;

    public NoArvoreB(boolean folha) {
        pares = new ArrayList<>();
        filhos = new ArrayList<>();
        eFolha = folha;
    }
}

public class ArvoreB {
    private NoArvoreB raiz;

    public ArvoreB() {
        raiz = new NoArvoreB(true);
    }

    public void inserir(int id, long endereco) {
        NoArvoreB r = raiz;
        if (r.pares.size() == 7) {
            NoArvoreB s = new NoArvoreB(false);
            raiz = s;
            s.filhos.add(r);
            dividirFilho(s, 0);
            inserirNaoCheio(s, id, endereco);
        } else {
            inserirNaoCheio(r, id, endereco);
        }
    }

    private void inserirNaoCheio(NoArvoreB x, int id, long endereco) {
        int i = x.pares.size() - 1;
        if (x.eFolha) {
            x.pares.add(new ParIdEndereco(id, endereco));
            while (i >= 0 && id < x.pares.get(i).id) {
                x.pares.set(i + 1, x.pares.get(i));
                i--;
            }
            x.pares.set(i + 1, new ParIdEndereco(id, endereco));
        } else {
            while (i >= 0 && id < x.pares.get(i).id) {
                i--;
            }
            i++;
            if (x.filhos.get(i).pares.size() == 7) {
                dividirFilho(x, i);
                if (id > x.pares.get(i).id)
                    i++;
            }
            inserirNaoCheio(x.filhos.get(i), id, endereco);
        }
    }

    private void dividirFilho(NoArvoreB x, int i) {
        NoArvoreB z = new NoArvoreB(x.filhos.get(i).eFolha);
        NoArvoreB y = x.filhos.get(i);
        x.filhos.add(i + 1, z);
        x.pares.add(i, y.pares.get(6));
        z.pares.addAll(y.pares.subList(7, 14));
        y.pares.subList(6, 14).clear();
        if (!y.eFolha) {
            z.filhos.addAll(y.filhos.subList(8, 15));
            y.filhos.subList(7, 15).clear();
        }
    }

    public boolean buscar(int id) {
        return buscar(raiz, id);
    }

    private boolean buscar(NoArvoreB x, int id) {
        int i = 0;
        while (i < x.pares.size() && id > x.pares.get(i).id) {
            i++;
        }
        if (i < x.pares.size() && id == x.pares.get(i).id) {
            return true;
        }
        if (x.eFolha) {
            return false;
        } else {
            return buscar(x.filhos.get(i), id);
        }
    }

    public static void main(String[] args) {
        ArvoreB arvoreB = new ArvoreB();
        arvoreB.inserir(1, 1000);
        arvoreB.inserir(3, 2000);
        arvoreB.inserir(7, 3000);
        arvoreB.inserir(10, 4000);
        arvoreB.inserir(11, 5000);
        arvoreB.inserir(13, 6000);
        arvoreB.inserir(14, 7000);
        arvoreB.inserir(15, 8000);
        arvoreB.inserir(18, 9000);
        arvoreB.inserir(16, 10000);
        arvoreB.inserir(19, 11000);
        arvoreB.inserir(24, 12000);
        arvoreB.inserir(25, 13000);
        arvoreB.inserir(26, 14000);
        arvoreB.inserir(21, 15000);
        arvoreB.inserir(4, 16000);
        arvoreB.inserir(5, 17000);
        arvoreB.inserir(20, 18000);
        arvoreB.inserir(22, 19000);
        arvoreB.inserir(2, 20000);
        System.out.println(arvoreB.buscar(20));
        System.out.println(arvoreB.buscar(21));
        System.out.println(arvoreB.buscar(5));
        System.out.println(arvoreB.buscar(0));
    }
}
