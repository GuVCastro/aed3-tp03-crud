import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Dados {
    // Funcao imprime mensagem (String) dentro de moldura
    private static void imprime_mensagem(String mensagem) {
        System.out.println("__________________________________________");
        System.out.println("\t" + mensagem);
        System.out.println("__________________________________________\n");
    }

    public void create_conta(String nome, String cpf, String cidade, Float saldo_inicial, RandomAccessFile fout) throws IOException {
        int ultimo_id, proximo_id;
        byte ba[];

        nome = nome.replaceAll("[ÁáÉéÍíÓóÚúÂâÊêÔôÃãÕõÇç]", "?");
        cidade = cidade.replaceAll("[ÁáÉéÍíÓóÚúÂâÊêÔôÃãÕõÇç]", "?");

        // Le Id do cabecalho (Id do novo objeto)
        fout.seek(0);
        ultimo_id = fout.readInt();

        // Instancia objeto para novo registro
        Contas conta = new Contas(ultimo_id, nome, cpf, cidade, 0, saldo_inicial);

        // conta.nome = criptografia_xor(conta.nome);

        // Escreve Id do ultimo registro acrescido de 1 no cabecalho
        fout.seek(0);
        proximo_id = ultimo_id + 1;
        fout.writeInt(proximo_id);

        // Converte objeto para vetor de bites
        ba = conta.toByteArray();
        fout.seek(fout.length());

        // Escreve registro no arquivo
        fout.writeByte(0);          // Lapide
        fout.writeInt(ba.length);   // Tamanho do registro
        fout.write(ba);             // Conteudo do registro

        imprime_mensagem("Cliente cadastrado com sucesso!\nDados: ");
        
        // conta.nome = criptografia_xor(conta.nome);

        System.out.println(conta);
    }

    public void update_conta(Contas conta_atualizada, RandomAccessFile fout) throws IOException {
        long pos;
        int tam_registro;
        byte lapide, ba[], conta_novo_registro[];
        boolean flag_encontrou = false;

        conta_atualizada.nome = conta_atualizada.nome.replaceAll("[ÁáÉéÍíÓóÚúÂâÊêÔôÃãÕõÇç]", "?");
        conta_atualizada.cidade = conta_atualizada.cidade.replaceAll("[ÁáÉéÍíÓóÚúÂâÊêÔôÃãÕõÇç]", "?");

        Contas conta = new Contas();

        // Le Id do ultimo registro
        fout.seek(0);
        fout.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho do registro
                pos = fout.getFilePointer();
                lapide = fout.readByte();
                tam_registro = fout.readInt();

                // Le dados de registro
                ba = new byte[tam_registro];
                fout.read(ba);
                conta.fromByteArray(ba);

                if(lapide == 0 && conta.id_conta == conta_atualizada.id_conta) {
                    flag_encontrou = true;

                    // conta_atualizada.nome = criptografia_xor(conta_atualizada.nome);

                    // Atualiza registro e converto para vetor de bytes
                    conta_atualizada.qtd_transferencias = conta.qtd_transferencias;
                    conta_novo_registro = conta_atualizada.toByteArray();

                    // Se tamanho do registro nao aumentou, escreve na mesma posicao
                    if(conta_novo_registro.length <= tam_registro) {
                        fout.seek(pos);

                        fout.readByte();
                        fout.readInt();
                        fout.write(conta_novo_registro);
                    }
                    // Se aumentou, deleta antigo registro e escreve novo no final do arquivo
                    else{
                        fout.seek(pos);
                        fout.writeByte(1);

                        fout.seek(fout.length());
                        fout.writeByte(0);
                        fout.writeInt(conta_novo_registro.length);
                        fout.write(conta_novo_registro);
                    }

                    imprime_mensagem("Conta atualizada!\nNovos dados: ");
                    System.out.println(conta_atualizada);

                    break;
                }
            }
            catch(EOFException err) {
                break;
            }
        }

        if(!flag_encontrou) {
            imprime_mensagem("Cliente não encontrado!");
        }
    }

    public void pesquisa_conta(int id_conta, RandomAccessFile fin) throws IOException {
        byte lapide;
        int tam_registro;
        byte ba[];
        boolean flag_encontrou = false;
        
        Contas conta = new Contas();

        // Le Id do ultimo registro
        fin.seek(0);
        fin.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho do registro 
                lapide = fin.readByte();
                tam_registro = fin.readInt();

                // Le dados do registro 
                ba = new byte[tam_registro];
                fin.read(ba);
                conta.fromByteArray(ba);

                // Se encontrar registro com Id inserido, mostre os dados 
                if(lapide == 0 && conta.id_conta == id_conta) {
                    flag_encontrou = true;

                    // conta.nome = criptografia_xor(conta.nome);

                    System.out.println("\n" + conta);
                    break;
                }
            }
            catch(EOFException err) {
                break;
            }
        }

        if(!flag_encontrou){
            imprime_mensagem("Cliente não encontrado!");
        }
    }

    public void delete_conta(int id_conta, RandomAccessFile fout) throws IOException{
        long pos;
        int tam_registro;
        byte lapide, ba[];
        boolean flag_encontrou = false;  
        
        Contas conta = new Contas();

        // Le Id do ultimo registro
        fout.seek(0);
        fout.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho do registro 
                pos = fout.getFilePointer();
                lapide = fout.readByte();
                tam_registro = fout.readInt();

                // Le dados do registro 
                ba = new byte[tam_registro];
                fout.read(ba);
                conta.fromByteArray(ba);

                // Se encontrar registro, alterar valor da lapide para 1 
                if(lapide == 0 && conta.id_conta == id_conta) {
                    flag_encontrou = true;

                    fout.seek(pos);
                    fout.writeByte(1);

                    imprime_mensagem("Conta removida!\n Dados removidos:");
                    System.out.println(conta);

                    break;
                }
            }
            catch(EOFException err) {
                break;
            }
        }

        if(!flag_encontrou) {
            imprime_mensagem("Cliente não encontrado!");
        }
    }

    public void transferencia_contas(int id_conta1, int id_conta2, float valor, RandomAccessFile fout) throws IOException {
        long pos_conta1 = -1, pos_conta2 = -1;
        int tam_registro;
        byte lapide, ba[];
        boolean flag1_encontrou = false, flag2_encontrou = false;
        DecimalFormat decimal = new DecimalFormat("#,##0.00");

        Contas conta1 = new Contas(), conta2 = new Contas(), conta = new Contas();

        // Le Id do ultimo registro
        fout.seek(0);
        fout.readInt();

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho de registro
                lapide = fout.readByte();
                tam_registro = fout.readInt();

                // Guarda posicao do registro caso Id correspondente ainda nao foi encontrado
                if(!flag1_encontrou) 
                    pos_conta1 = fout.getFilePointer();
                if(!flag2_encontrou) 
                    pos_conta2 = fout.getFilePointer();

                // Le dados do registro 
                ba = new byte[tam_registro];
                fout.read(ba);
                conta.fromByteArray(ba);

                // Registra dados em objeto correspondente se Id for encontrado
                if(lapide == 0) {
                    if(conta.id_conta == id_conta1) {
                        flag1_encontrou = true;

                        conta1.fromByteArray(ba);
                    }
                    else if(conta.id_conta == id_conta2) {
                        flag2_encontrou = true;

                        conta2.fromByteArray(ba);
                    }
                }

                if(flag1_encontrou && flag2_encontrou)
                    break;
            }
            catch(EOFException err) {
                break;
            }
        }

        // Registros de ambas as contas devem ser encontrados
        if(!(flag1_encontrou && flag2_encontrou)) {
            imprime_mensagem("Cliente não encontrado!");       
        }
        else {
            // Atualiza saldo e qtd de transferencias das contas 
            conta1.saldo -= valor;
            conta2.saldo += valor;
            conta1.qtd_transferencias++;
            conta2.qtd_transferencias++;

            // Escreve valores atualizados no arquivo
            fout.seek(pos_conta1);
            fout.write(conta1.toByteArray());
            fout.seek(pos_conta2);
            fout.write(conta2.toByteArray());

            imprime_mensagem("Transferência realizada!\nDados: ");
            System.out.println("Cliente (origem): " + conta1.nome + "\nValor tranferido: R$ " + decimal.format(valor) + "\nCliente (destino): " + conta2.nome);
        }
    }

    public void compactar_arquivo(RandomAccessFile arq) throws IOException {
        RandomAccessFile arq_versao = new RandomAccessFile("versao.bin", "rw");
        
        int versao = 0;
        try {
            arq_versao.seek(0);
            versao = arq_versao.readInt();
        } catch (Exception e) {
            versao = 0;
            arq_versao.seek(0);
            arq_versao.writeInt(0);
        }

        RandomAccessFile arq_destino = new RandomAccessFile("contasCompressao" + versao + ".lzw", "rw");

        versao++;
        arq_versao.seek(0);
        arq_versao.writeInt(versao);
        arq_versao.close();

        byte    lapide,
                ba[];
        int tam_registro;
        Contas conta = new Contas();
        Lzw lzw = new Lzw();

        // Le Id do ultimo registro
        arq.seek(0);
        arq_destino.seek(0);
        arq_destino.writeInt(arq.readInt());

        while (true) {
            try {
                // Le cabecalho do registro 
                lapide = arq.readByte();
                // Le dados do registro 
                tam_registro = arq.readInt();
                ba = new byte[tam_registro];
                arq.read(ba);

                if (lapide != 1) {
                    conta.fromByteArray(ba);

                    List<Integer>   result_nome = new ArrayList<>(),
                                    result_cpf = new ArrayList<>(),
                                    result_cidade = new ArrayList<>();

                    result_nome = lzw.compressao_lzw(conta.nome);
                    result_cpf = lzw.compressao_lzw(conta.cpf);
                    result_cidade = lzw.compressao_lzw(conta.cidade);

                    byte ba_comprimido[] = lzw.toByteArrayLzw(  conta.id_conta, result_nome, result_cpf, 
                            result_cidade, conta.qtd_transferencias, conta.saldo);
                    
                    arq_destino.writeByte(ba_comprimido.length);
                    arq_destino.write(ba_comprimido);
                }
            } catch (EOFException err) {
                arq_destino.close();
                break;
            }
        }

        System.out.println("Arquivo Compactado com sucesso!");

        Path path_comprimida = Paths.get("contasCompressao" + (versao - 1) + ".lzw"),
            path_original = Paths.get("contas.bin");
        float tamanho_original = Files.size(path_original),
            tamanho_comprimido = Files.size(path_comprimida);

        System.out.println("Redução no espaço: " + (100 - (tamanho_comprimido/tamanho_original) * 100) + "%");
    }

    public void descompactar_arquivo(int versao) throws IOException {
        RandomAccessFile arq_versao = new RandomAccessFile("versao.bin", "rw");

        try {
            arq_versao.seek(0);
            arq_versao.readInt();
        } catch (Exception e) {
            System.out.println("Versão Inválida!");
            arq_versao.close();
            return;
        }

        arq_versao.seek(0);
        if (versao > (arq_versao.readInt() - 1)) {
            System.out.println("Versão Inválida!");
            arq_versao.close();
            return;
        }

        RandomAccessFile arq = new RandomAccessFile("contasCompressao" + versao + ".lzw", "rw");        
        RandomAccessFile arq_destino = new RandomAccessFile("contas.bin", "rw");

        if (arq.length() == 0) {
            new RandomAccessFile("contas.bin", "rw").setLength(0);
            System.out.println("Arquivo Não Encontrado!");
            arq.close();
            return;
        } else if (arq_destino.length() == 0) {
            new RandomAccessFile("contasCompressao" + versao + ".lzw", "rw").setLength(0);
            System.out.println("Arquivo Não Encontrado!");
            arq_destino.close();
            return;
        }
        
        arq_versao.close();

        byte    tam_registro,
                ba[];
        Contas conta = new Contas();
        Lzw lzw = new Lzw();

        // Le Id do ultimo registro
        arq.seek(0);
        arq_destino.seek(0);
        arq_destino.writeInt(arq.readInt());

        // Verifica todos os registros ate encontrar Id inserido
        while(true) {
            try {
                // Le cabecalho do registro 
                arq_destino.writeByte(0);
                tam_registro = arq.readByte();

                // Le dados do registro 
                ba = new byte[tam_registro];
                arq.read(ba);
                conta = lzw.fromByteArrayLzw(ba);

                ba = conta.toByteArray();
                arq_destino.writeInt(ba.length);
                arq_destino.write(ba);
            } catch (EOFException err) {
                break;
            }
        }

        System.out.println("Arquivo Descompactado com Sucesso!");
    }
}
