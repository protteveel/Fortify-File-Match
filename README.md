# Fortify-File-Match
Creates an overview of Fortify SAST configured and non-configured file extensions within a given directory.

Point it to a directory and it will provide an overview of the files that will be scanned by Fortify SAST.

## Usage: 
java -jar ./ScaFileMatch.jar <root directory>
  
## Example:
java -jar ./ScaFileMatch.jar /Users/percyrotteveel/workspace/ScaFileMatch

## Sample:
List of Fortify SCA configured file extensions:
|Extension |No files |No lines   |Type           |
|----------| -------:|----------:|---------------|
|html      | 46 files|  734 lines|HTML           |
|java      |103 files|30574 lines|JAVA           |
|js        |  5 files|  252 lines|JAVASCRIPT     |
|jsp       | 27 files| 1787 lines|JSP            |
|properties|  1 files|    1 lines|JAVA_PROPERTIES|
|wsdd      |  1 files|   69 lines|XML            |
|xml       |  5 files| 1323 lines|XML            |
|Totals    |188 files|34740 lines|               |

List of Fortify SCA non-configured file extensions:
|Extension |No files    |
|---------:|-----------:|
|       css|    8 files |
|       gif|    2 files |
|       jar|   15 files |
|       jpg|   51 files |
|       mdb|    1 files |
|       mf |    1 files |
|       prp|    1 files |
|       txt|    1 files |
| Totals   |    80 files|
