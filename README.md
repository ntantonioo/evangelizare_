# Evangelizare 

**Sistema de Apoio à Catequese**

O **Evangelizare** é um projeto desenvolvido em Java com o objetivo de auxiliar na organização das atividades de catequese, facilitando o gerenciamento de usuários, turmas e notícias da comunidade catequética.

O sistema busca organizar as informações e definir permissões de acordo com o perfil de cada usuário, promovendo uma gestão mais simples e eficiente da catequese.

## 🎯 Objetivos

- Organizar os cadastros de catequistas e catequisandos.
- Gerenciar turmas de catequese.
- Controlar o acesso às funcionalidades conforme o perfil do usuário.
- Centralizar notícias e informações relevantes para a comunidade catequética.
- Aplicar conceitos de Programação Orientada a Objetos (POO) e boas práticas de desenvolvimento.

## ⚙️ Funcionalidades

- [x] Estrutura inicial do sistema.
- [x] Menu principal interativo no terminal.
- [x] Modelagem de usuários e turmas.
- [x] Definição dos perfis de usuário e das regras de acesso.
- [x] Estrutura para gerenciamento de turmas e usuários.
- [x] Estrutura para autenticação de usuários.
- [ ] Concluir e validar o fluxo completo de autenticação.
- [ ] Finalizar e testar os fluxos de cadastro.
- [ ] Implementar o gerenciamento completo de notícias.
- [ ] Desenvolver uma interface gráfica.

> O projeto está em desenvolvimento. A lista de funcionalidades será atualizada conforme novas etapas forem concluídas.

## 👥 Perfis de usuário

O sistema contempla três perfis principais:

| Perfil | Permissões previstas |
|---|---|
| **Administrador** | Cadastrar catequistas, gerenciar turmas e publicar notícias. |
| **Catequista** | Cadastrar catequisandos e acompanhar as informações relacionadas à catequese. |
| **Catequisando** | Consultar informações e notícias disponibilizadas pelo sistema. |

## 📚 Tipos de turma

O sistema contempla diferentes modalidades de catequese:

- Catequese Infantil
- Crisma Jovem
- Crisma Adulto

## 🛠️ Tecnologias e conceitos

- **Java** — linguagem de programação.
- **Programação Orientada a Objetos (POO)** — organização do sistema em classes, objetos, interfaces e enums.
- **Collections Framework** — gerenciamento de dados em coleções.
- **Tratamento de exceções** — identificação e tratamento de situações excepcionais.
- **Git e GitHub** — versionamento e gerenciamento do código-fonte.

## 🏗️ Organização do projeto

O código é organizado em pacotes de acordo com suas responsabilidades.

| Pacote | Responsabilidade |
|---|---|
| `model` | Representação das entidades do sistema, como usuários, turmas e notícias. |
| `service` | Implementação das regras de negócio e operações do sistema. |
| `exception` | Exceções específicas para situações como registros não encontrados. |
| `ui` | Interface de interação com o usuário, atualmente por meio do terminal. |

Essa separação busca facilitar a manutenção, a leitura e a evolução do código.

---

*Evangelizare — tecnologia a serviço da comunidade catequética.*
