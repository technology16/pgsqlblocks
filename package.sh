#!/bin/bash
# Скрипт для авторелиза на guthub с обращением к GitHub APIv3
###################################################################################################################
#Создание авторелиза. Весь процесс создания релиза происходит в два этапа:
# 1. Подготовка к релизу.
#   а. Генерация файлов для релиза с использованием mvn
#   б. Получение необходимой информации о будущем релизе из CHANGELOG.md
# 2. Релиз.
#   а. Формирование тела запроса в формате JSON
#   б. Создание тела релиза
#   в. Отправка ассетов на сайт
####################################################################################################################
# Как использовать:
# ./package.sh -t=ТУТТОКЕНКГИТХАБУ -rn="НАЗВАНИЕ РЕЛИЗА"
#
# Параметры:
# -t=ТУТТОКЕНКГИТХАБУ     Access token пользователя (https://github.com/settings/tokens)
# -tf Флаг о том, чтобы использовать токен, находящийся в файле github.token
# -rn="НАЗВАНИЕ РЕЛИЗА"   Название релиза, его краткое описание (НЕ название тэга)
####################################################################################################################
# Возможные обязательные/необязательные изменения в файле
# TARGET_COMMITISH, DRAFT, PRERELEASE
# !!!!Владелец репозитория OWNER_OF_REPO
# !!!!Название репозитория PROJECT
# Список файлов для релиза
####################################################################################################################
# В случае некорректной сборки mvn скрипт крашится
####################################################################################################################

# Переменная получающая из pom.xml версию проекта
version=$(mvn help:evaluate -Dexpression=project.version | grep '^[0-9][0-9A-Za-z\.\-]*$')

# Чтение аргументов
echo "ARGUMENTS"
for i in "$@"
do
case $i in
    -t=*|--token=*)
    TOKEN="${i#*=}"
    shift
    ;;
    -tf|--token-file)
    TOKEN=`cat github.token`
    shift
    ;;
    -rn=*|--releasename=*)
    RELEASE_NAME="${i#*=}"
    echo "RELEASE NAME"
    shift
    ;;
esac
done
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Перечень переменных от которых зависит корректная отправка релиза
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Токен доступа, генерируется в настройках профиля. Достаточно права repo
# Записывается в файл в виде 165434643515пиввапв

GIT_TOKEN=${TOKEN}
# Здесь ветка, либо SHA
TARGET_COMMITISH="master"
# Название релиза
NAME_OF_RELEASE="$RELEASE_NAME"
# Если true, то релиз непубличный, по умолчанию всегда публичный и false
DRAFT=false
# Если true, то это пререлиз
PRERELEASE=false
# Владелец репозитория
OWNER_OF_REPO="technology16"
# Название репозитория
PROJECT="pgsqlblocks"

# Создаем временные файлы и пишем в переменную
some=$(mktemp /tmp/some.XXXXXX)
file=$(mktemp /tmp/file.XXXXXX)
file2=$(mktemp /tmp/file2.XXXXXX)
descr=$(mktemp /tmp/descr.XXXXXX)

clean_tmp_files(){
    rm $some;
    rm $file;
    rm $file2;
    rm $descr;
}
# Функция для нахождения в CHANGELOG.md номера предыдущей версии
# сначала игнорим (-v) текущую версию и отбираем следующее первое совпадение
get_last_version(){
grep -v $version CHANGELOG.md | grep -m 1 "^[0-9]*\.[0-9]*"
}
LAST=$(get_last_version)

# Функция получения описания релиза из CHANGELOG.md
# выбирает весь текст между, например, 1.0.0 и 0.0.9 и пишет в файл some.md
# так как в файл пишется вместе с ненужными номерами версий, грепаем эти строки и пишем в переменную
get_descr(){
sed -n  "/"$version"/,/"$LAST"/p" CHANGELOG.md > ${some}
grep -v  "^[0-9]*\.[0-9]*" ${some} > ${descr}
}
$(get_descr)

description=`cat ${descr}`

# Тело запроса на создание тега релиза
API_JSON='{"tag_name": "v'$version'","target_commitish": "'$TARGET_COMMITISH'","name": "'$NAME_OF_RELEASE'","body": "'$description'","draft": '$DRAFT',"prerelease": '$PRERELEASE'}'
echo "$API_JSON" > ${file}
# Так как описание содержит в себе явные переносы, а парсер апи их распознает некорректно заменяем их на "\n"
sed -E ':a;N;$!ba;s/\r{0,1}\n/\\n/g' ${file} > ${file2}

echo Описание релиза
echo Тэг v$version
echo Название релиза $RELEASE_NAME
echo Changelog:
echo ==============================
cat ${descr}
echo ==============================

read -p "Собрать релиз? [y/n]" -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    $(clean_tmp_files);
    exit 1;
fi

echo

# Build files for release
mvn clean -q

echo "Выполняются тесты..."
mvn test -q || { echo; echo 'Ошибка в тестах, деплой отменен!' ; $(clean_tmp_files); exit 1;}

echo "Идет сборка Windows-32..."
mvn package -P Windows-32 -q -DskipTests

echo "Идет сборка Windows-64..."
mvn package -P Windows-64 -q -DskipTests

echo "Идет сборка Linux-32..."
mvn package -P Linux-32 -q -DskipTests

echo "Идет сборка Linux-64..."
mvn package -P Linux-64 -q -DskipTests

echo "Идет сборка Macosx-64..."
mvn package -P Macosx-64 -q -DskipTests

echo "Сборка завершена"

# Проверка корректности сборки проекта
FILENAME="pgSqlBlocks"
TARGET="./target"
platformArray=("Linux-32" "Linux-64" "Macosx-64" "Windows-32" "Windows-64")

for platform in ${platformArray[*]}
do
   if [ ! -f ""$TARGET"/"$FILENAME"-"$version"-"$platform".jar" ]; then
       echo ">>>>>>>>>>>>>!!!!!отсутствует файл под "$platform""
       $(clean_tmp_files)
       exit 1;
   fi
done

GH_URL="https://api.github.com/repos/$OWNER_OF_REPO/$PROJECT/releases?access_token=$GIT_TOKEN"
# Отправляем запрос, в ответ приходит JSON с полным описанием релиза. Из него получаем id релиза.
response=$(curl -H "Content-Type: application/json" --data-binary @"$file2" $GH_URL)
# check status
check_errors=$(grep -q -m 1 "errors:" <<< ${response})
    if [[ "${check_errors}" == "errors:" ]]; then
        echo ${response}
        $(clean_tmp_files)
        exit 1;
    fi
# Приводим id релиза в нормальный вид. ID нужен для загрузки ассетов
get_id=$(grep -m 1 "id.:" <<< "$response")
id=$(grep -o '[[:digit:]]*' <<< "$get_id")
echo "Тэг для релиза был создан"
# Construct url
echo "Uploading assets..."
# Проходим по массиву ассетов и загружаем их на сайт
for platform in ${platformArray[*]}
do
    filename=${FILENAME}"-"${version}"-"${platform}".jar"
    echo Uploading $filename
    file=${TARGET}"/"${filename}
    GH_ASSET="https://uploads.github.com/repos/"$OWNER_OF_REPO"/"$PROJECT"/releases/"$id"/assets?name="${filename}""
    upload_responce=$(curl -sS -H "Authorization: token $GIT_TOKEN" -H "Content-Type: application/octet-stream" --write-out "HTTPSTATUS:%{http_code}" --data-binary @"$file"  $GH_ASSET)
    upload_status=$(echo $upload_responce | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    if [ ! $upload_status -eq 201  ]; then
        upload_body=$(echo $upload_responce | sed -e 's/HTTPSTATUS\:.*//g')
        echo ${upload_body}
       fi
done
$(clean_tmp_files)
git fetch --tags

x-www-browser https://github.com/$OWNER_OF_REPO/$PROJECT/releases/latest