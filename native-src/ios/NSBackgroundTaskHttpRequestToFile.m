
#import "NSBackgroundTask.h"
#import "AFNetworking.h"


@implementation NSBackgroundTaskHttpRequestToFile

@synthesize delegate;

-(void) runTask{
	dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
	    
		
		NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
		AFURLSessionManager *manager = [[AFURLSessionManager alloc] initWithSessionConfiguration:configuration];	
	    NSFileManager *fileManager = [NSFileManager defaultManager];
	    NSURL *url = [[NSURL alloc] initWithString: _url];
	    
	    NSURLRequest *request = [NSURLRequest requestWithURL:url];
	    	    
	    NSString *destination = _toFile;

	    //NSArray *listItems = [destination componentsSeparatedByString:@", "];

	    //NSString *fileName = (NSString *)[listItems lastObject];
	    //NSString *destinationFile = [destination stringByAppendingPathComponent: fileName];

	    if([fileManager fileExistsAtPath: destination ] == YES){
	    	NSLog(@"deleting destination file %@ before download..", destination);
	    	NSError *deleteError;
	    	[fileManager removeItemAtPath: destination error:&deleteError];

			if(deleteError){
				NSLog(@"error on delete file to %@ -> %@", destination, deleteError);
				[self.delegate onError: [deleteError description]];
				return;
			}

	    }

	    NSLog(@"dowload file from %@", _url);
	    
				
		NSURLSessionDownloadTask *downloadTask = [manager downloadTaskWithRequest:request progress:nil destination:^NSURL *(NSURL *targetPath, NSURLResponse *response) {
		    NSURL *documentsDirectoryURL = [[NSFileManager defaultManager] URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:NO error:nil];
		    return [documentsDirectoryURL URLByAppendingPathComponent:[response suggestedFilename]];
		} completionHandler:^(NSURLResponse *response, NSURL *filePath, NSError *error) {

			if(error){
				NSLog(@"dowload error %@", error);
				[self.delegate onError: [error description]];
			}else{
				NSLog(@"dowload error success!");
				NSError *moveError;
				[fileManager moveItemAtPath: filePath toPath: destination error: &moveError];

				if(moveError){
					NSLog(@"error dowload move %@ to %@ -> %@", filePath, destination, moveError);
					[self.delegate onError: [moveError description]];
				}else{
					NSLog(@"success dowload move %@ to %@", filePath, destination);
		    		[self.delegate onComplete];
		    	}
		    }
		}];

		[downloadTask resume];

	});
}


-(id) initWithUrl:(NSString *) url toFile: (NSString *) toFile{
	self = [super init];	
	_toFile = toFile;
	_url = url;	
	return self;
}

@end