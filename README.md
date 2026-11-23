# API Cifra de César

API REST em Java (Spring Boot) que criptografa e descriptografa textos usando a Cifra de César. Também possui um endpoint que descriptografa por **força bruta**, sem precisar informar o deslocamento.

## Integrantes

- Enzo Eiji Banin
- Mateus Gois Silva

## API hospedada

| Função | Método | URL completa |
|---|---|---|
| Criptografar | POST | `https://cifra-cesar-api-cc61.onrender.com/cifrar` |
| Descriptografar | POST | `https://cifra-cesar-api-cc61.onrender.com/decifrar` |
| Descriptografar por força bruta | POST | `https://cifra-cesar-api-cc61.onrender.com/decifrarForcaBruta` |

## Como a cifra funciona

- **Deslocamento positivo** desloca as letras **para frente** no alfabeto; **negativo**, **para trás**.
- Ao **descriptografar**, a API aplica o deslocamento inverso. Usar o mesmo deslocamento no `/cifrar` e no `/decifrar` devolve o texto original.
- Só as letras de `A` a `Z` e de `a` a `z` são deslocadas. Maiúsculas e minúsculas são preservadas.
- Espaços, números, pontuação, símbolos e **letras acentuadas** (como `ç` e `ã`) **não são alterados**.
- Qualquer valor inteiro de deslocamento é aceito. O alfabeto "dá a volta": com deslocamento 3, `x` vira `a`; com -3, `a` vira `x`.

## Endpoints

### `POST /cifrar`

Requisição:
```json
{
  "textoClaro": "xyz",
  "deslocamento": 3
}
```

Resposta (200):
```json
{
  "textoCifrado": "abc"
}
```

### `POST /decifrar`

Requisição:
```json
{
  "textoCifrado": "abc",
  "deslocamento": 3
}
```

Resposta (200):
```json
{
  "textoClaro": "xyz"
}
```

### `POST /decifrarForcaBruta`

Recebe apenas o texto cifrado, sem o deslocamento, e devolve a tentativa que faz sentido como texto em português.

Requisição:
```json
{
  "textoCifrado": "FMHC RCK YSJY"
}
```

Resposta (200):
```json
{
  "textoClaro": "HOJE TEM AULA"
}
```
## Erros

Requisições inválidas retornam **HTTP 400**, por exemplo:

- texto vazio ou ausente;
- deslocamento ausente ou que não seja um número inteiro;
- JSON malformado.

## Como executar localmente

**Requisitos:** JDK 17 ou superior. Não é preciso instalar o Maven, pois o projeto inclui o Maven Wrapper.

Na pasta raiz do projeto (onde está o `pom.xml`):

- **Windows:**
  ```
  mvnw.cmd spring-boot:run
  ```
- **Linux / macOS:**
  ```
  ./mvnw spring-boot:run
  ```

A API sobe na porta **8080**:

| Função | URL local |
|---|---|
| Criptografar | `http://localhost:8080/cifrar` |
| Descriptografar | `http://localhost:8080/decifrar` |
| Força bruta | `http://localhost:8080/decifrarForcaBruta` |
