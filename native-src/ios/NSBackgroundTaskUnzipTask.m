
#import "NSBackgroundTask.h"
#import "SSZipArchive.h"

@implementation NSBackgroundTaskUnzipTask

@synthesize delegate;

-(void) runTask{
	dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
	    
	    @try{
	    	NSFileManager *fileManager = [NSFileManager defaultManager];
	    	NSLog(@"unzip file %@ to %@", _fromFile, _toFile);

	    	/*
	    	if([fileManager fileExistsAtPath: destination ] == NO){
	    		NSError *createError;
				[fileManager createDirectoryAtPath:destination withIntermediateDirectories:NO
				                                                        attributes:nil
				                                                             error:&createError];
				if(createError){
					NSLog(@"create path error %@", createError);
					return;
				}	    		
	    	}*/

	    	[SSZipArchive unzipFileAtPath:_fromFile toDestination:_toFile];
	    	[self.delegate onComplete];
	    }@catch(NSException *exception){
	    	NSLog(@"move file error %@", exception);
	    	[self.delegate onError: [exception reason]];
	    }	

	});
}


-(id) initWithFromFile:(NSString *) fromFile toFile: (NSString *) toFile{
	self = [super init];	
	_toFile = toFile;
	_fromFile = fromFile;	
	return self;
}

@end