package arvore;

import java.io.RandomAccessFile;

public class ArvoreBMais {
    RandomAccessFile raf;
    int ordem;
    int enderecoRaiz;

    public ArvoreBMais(String nomeArquivo, int ordem) {
        try {
            raf = new RandomAccessFile(nomeArquivo, "rw");
            raf.seek(0);
            this.ordem = ordem;
            if(raf.length() == 0) {
                raf.writeInt(-1);
            } else {
                enderecoRaiz = raf.readInt();
                this.ordem = raf.readInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pagina inserir(int id, long pos) throws Exception{
        raf.seek(0);
        enderecoRaiz = raf.readInt();
        if(enderecoRaiz == -1){
            Pagina novaPagina = new Pagina(ordem);
            novaPagina.ids[0] = id;
            novaPagina.posicoes[0] = pos;
            novaPagina.qtdElementos++;
            novaPagina.isFolha = true;
            novaPagina.pos = (int)raf.length();
            raf.seek(novaPagina.pos);
            novaPagina.salvar(raf, ordem);
            raf.seek(0);
            raf.writeInt(novaPagina.pos);

            return novaPagina;
        }

        raf.seek(enderecoRaiz);
        Pagina raiz = new Pagina(raf, ordem);
        long[] res = raiz.inserir(id, pos, raf, ordem);

        if(res == null){
            return raiz;
        }

        Pagina novaPagina = new Pagina(ordem);
        novaPagina.ids[0] = (int)res[0];
        novaPagina.posicoes[0] = res[1];
        novaPagina.qtdElementos++;
        novaPagina.isFolha = false;
        novaPagina.filhos[0] = enderecoRaiz;
        novaPagina.filhos[1] = (int)raf.length();
        novaPagina.pos = (int)raf.length();
        raf.seek(novaPagina.pos);
        novaPagina.salvar(raf, ordem);
        raf.seek(0);
        raf.writeInt(novaPagina.pos);

        return novaPagina;
    }

    public void print() throws Exception{
        System.out.println("\nRaiz: " + enderecoRaiz);
        System.out.println();
        raf.seek(4);
        while(raf.getFilePointer() < raf.length()){
            Pagina pagina = new Pagina(raf, ordem);
            System.out.println(pagina);
        }

    }
}