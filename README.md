### key, value 설정
* key = 엔티티이름(소문자):pk 제외 식별 가능한 값
* value = 엔티티 데이터 그대로

### 레디스 키 만들때 유의점
1. "도메인 이름::메서드 이름::파라미터 문자열 직렬화" 하여 키로 설정하고 있는데
   1. 파라미터가 직접 만든 클래스이고 필드값이 있을 경우 문제가 없지만
   2. 자바 기본 클래스인 경우(String, Integer) 같은 로직으로 직렬화할 경우 기본 클래스 필드 값들에 접근하게 된다. 
      원하는 결과가 String 인 경우 예를 들면 String 클래스 안의 필드값을 읽어오는 경우가 생김
   3. 자바 기본 클래스들의 패키지 명은 java.lang 이므로 getName() 메서드를 사용하여 패키지 명을 확인하고
      1. 기본 클래스인 경우 simpleName:해당값
      2. 커스텀 클래스인 경우 기존 로직 사용

## 자세한 설명
### Datasource Transaction 적용하기
1. @EnableTransactionManagement 어노테이션을 활성화 시켜주고
2. jdbc or jpa PlatformTransactionManager 를 반환해주는 빈을 설정한다.

### Serializer 설정하기

* GenericJackson2JsonRedisSerializer
  * 객체의 클래스 지정없이 직렬화해준다는 장점을 가지고있다.
  * 단점으로는 Object 의 class, package 까지 전부 함께 저장하게 되어
  다른 프로젝트에서 저장된 값을 사용하려면 package 까지 일치 시켜줘야한다.
* Jackson2JsonRedisSerializer
  * 클래스를 지정해줘야해서 redis 에 객체를 저장할 때 class 값을 저장하지 않는다.
  package 등이 일치할 필요가 없는 장점이 있다.
  * class 타입을 지정하기 때문에 여러 쓰레드에서 접근하게 될때 serializer 타입의 문제가 발생할 수 있다.
* StringRedisSerializer
  * JSON 형태로 직접 encoding, decoding 해줘야 하는 단점이 있지만
  다른 문제들이 발생하지 않는다.
  1. class 타입의 지정이 필요하지 않다.
  2. package 까지 일치할 필요가 없다.
  3. 쓰레드간에 문제가 발생하지 않는다.

RedisTemplate 빈 주입시 key, value, hashkey, hashvalue 들에서 StringRedisSerializer 를 사용하게 설정한다.

### JSON Parsing
Object -> JSON, JSON -> Object 파싱해주는 유틸 클래스를 만들어 주어 사용한다.

# gascharge-module-redis
