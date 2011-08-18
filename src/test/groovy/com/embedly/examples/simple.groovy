package com.embedly.examples

import com.embedly.api.Api
import org.apache.commons.logging.LogFactory

class Simple {
	static void main(args) {
		Api.setLog(LogFactory.getLog(Api.class))
		def params = [
			urls: [
				'http://www.youtube.com/watch?v=sPbJ4Z5D-n4&feature=topvideos',
				'http://www.guardian.co.uk/media/2011/jan/21/andy-coulson-phone-hacking-statement'
			]
		]
		def api = new Api('blah blah blah', System.getenv()['EMBEDLY_KEY']);
		print api.oembed(params)
	}
}
