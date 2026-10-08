# Crystalline Dynamo no Blockbench

Abra `crystalline_dynamo.bbmodel` pelo **File > Open Model** do Blockbench. Esse é o projeto editável da carcaça. As demais peças têm projetos separados porque o mod as renderiza e anima separadamente:

| Projeto | Peça no jogo |
| --- | --- |
| `crystalline_dynamo.bbmodel` | Carcaça, fornalha e encaixe traseiro |
| `left_jaw.bbmodel`, `right_jaw.bbmodel` | Mandíbulas da moagem |
| `shaft.bbmodel` | Eixo cinético traseiro |
| `fire.bbmodel`, `superheated_fire.bbmodel` | Chamas que aparecem durante a moagem |

Depois de editar, use **File > Save Project** no Blockbench. Para atualizar os JSONs carregados pelo Minecraft, execute na raiz do projeto:

```powershell
python scripts/sync_crystalline_dynamo_blockbench.py --export
```

`--check` verifica se os projetos do Blockbench e os JSONs estão sincronizados. **Não use `--bootstrap` depois de editar no Blockbench**: essa opção recria os projetos a partir dos JSONs e descarta as edições ainda não exportadas.

Os projetos incluem cópias embutidas das texturas para abrir com a aparência correta. A exportação acima atualiza a geometria e o UV; se pintar uma textura no Blockbench, exporte o PNG editado separadamente para `src/main/resources/assets/actuallycreated/textures/block/crystalline_dynamo/`. As texturas referenciadas diretamente do Create continuam sendo fornecidas pelo Create.

Ao editar o encaixe traseiro, deixe espaço para a rotação do shaft. As animações e a troca de chamas são controladas por `CrystallineDynamoRenderer.java`, fora dos modelos.
