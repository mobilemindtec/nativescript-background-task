
# Intranet KalyKim


## instalação (do zero)

Executar no mesmo nível do arquivo docker-compose.yaml

`$ sudo docker-compose build`

Após a criação da vm docker, é necessário importar o arquivo do banco de dados.

Na maquina física os arquivos estarão em:

/opt/apps/intranet/documentos - documentos armazenados
/opt/apps/intranet/datadir - Arquivos MySQL

## Copiar banco de dados pra vm docker

`sudo docker cp sql/intranetklm.sql  intranet_dbserver:/code/extra`

## Importar o dump do banco de dados

Acesse a vm do banco e execute

Usuáiro: root
Senha: 1NtR@n3t

Caso tenha seguido o passo anterior, o arquivo vai estar em `/code/extra`

## Executar aplicação para produção

`$ sudo docker-compose start`

## Acessar banco de dados

A aplicação deve estar rodando. Banco de dados MySQL

`$ sudo docker exec -i -t intranet_dbserver /bin/bash`

## Acessar web server

A aplicação deve estar rodando. Nginx + NodeJs

`sudo docker exec -i -t intranet_webserver /bin/bash`


## Email usado para envio de emails

Usuáiro: intranetklm@gmail.com
Senha: klm@1ntR@n3t


## Preparar amniente

Instale o mysql e crie um banco chamado intranet. As configurações do banco podem ser encontrada em `dev/config/config.json`
Instele o `nvm` e use node versão 8

Entre na pasta dev e execute 

```

$ npm install
$ npm install -g nodemon
$ npm install -g sequelize-cli
$ npm install -g mysql
$ npm install -g mysql2
$ npm install -g gulp
$ npm install -g coffescript

```

Criar dados padrões no banco de dados

```
// cria
$ sequelize db:seed:all 
// apaga
$ sequelize db:seed:undo:all
```

A configuração da pasta de documento pode ser econtrada em 

`dev/env.coffee`

Executar em modo de desenvolvimento

Em um terminal entre na pasta dev e execute

```
$ gulp watch
```

Em outro terminal entre na pasta dist e execute

```
$ nodemon bin/www.js
```

## Compilar aplicação

Entrar na pasta `dev` e executar `gulp`. A aplicação compilada e pronta para publicação estará na pasta `dist`.
