# embedly-java

Embedly java client library.  To find out what Embedly is all about, please
visit http://embed.ly.  To see our api documentation, visit
http://api.embed.ly/docs.

## Installing

We keep the latest release on MavenCentral.

    <dependency>
        <groupId>com.embedly</groupId>
        <artifactId>embedly-api</artifactId>
        <version>0.1.2</version>
    </dependency>

If you would like cutting edge, then you can clone and install HEAD.

    git clone git@github.com:dokipen/embedly-java.git
    cd embedly-java
    gradle build

## Getting Started

    /* Demo.java */

    import java.util.HashMap;

    import org.json.JSONArray;

    import org.apache.commons.logging.LogFactory;

    import com.embedly.api.Api;

    public class Demo {
      public static void main(String[] args) {
          Api.setLog(LogFactory.getLog(Api.class));
          Api api = new API('Mozilla/5.0 (compatible; mytestapp/1.0; my@email.com)');

          HashMap<String, Object> params = new HashMap<String, Object>();
          params.put('url', 'http://www.youtube.com/watch?v=sPbJ4Z5D-n4&feature=topvideos');

          JSONArray json = api.oembed(params);
          System.out.println(""+json);

          System.out.println("------------------------------------------------------");

          ArrayList<String> urls = new ArrayList<String>();
          urls.add('http://www.youtube.com/watch?v=sPbJ4Z5D-n4&feature=topvideos');
          urls.add('http://twitpic.com/3yr7hk');
          params = new HashMap<String, Object>();
          params.put('urls', urls);

          JSONArray json = api.oembed(params);
          System.out.println(""+json);

          System.out.println("------------------------------------------------------");

          api = new Api('Mozilla/5.0 (compatible; mytestapp/1.0; my@email.com)',
                        'xxxxxxxxxxxxxxxxxxxxxxxxxxx'); // <-- put key here
          params = new HashMap<String, Object>();
          params.put('url', 'http://www.guardian.co.uk/media/2011/jan/21/andy-coulson-phone-hacking-statement');

          JSONArray json = api.oembed(params);
          System.out.println(""+json);

      }
    }

## Testing

    gradle test
    EMBEDLY_KEY=xxxxxxxxxxxxx gradle runFeatures

## Release

    Add -Drelease=true -Dsign=true to gradle command line.  Be sure
    gradle.settings has the appropriate settings and that version is
    not SNAPSHOT.

## Note on Patches/Pull Requests

* Fork the project.
* Make your feature addition or bug fix.
* Add tests for it. This is important so I don't break it in a
  future version unintentionally.
* Commit, do not mess with rakefile, version, or history.
  (if you want to have your own version, that is fine but bump version in a commit by itself I can ignore when I pull)
* Send me a pull request. Bonus points for topic branches.

## Copyright

Copyright (c) 2011 Embed.ly, Inc. See LICENSE for details.
