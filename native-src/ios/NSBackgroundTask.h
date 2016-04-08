

@protocol NSBackgroundTaskCompleteCallback

-(void) onComplete;
-(void) onError:(NSString *) message;

@end


@interface NSBackgroundTaskCopyFiles : NSObject{
NSString *_toFile;
NSString *_fromFile;
}

@property (nonatomic, retain) id<NSBackgroundTaskCompleteCallback> delegate;

-(void) runTask;

-(id) initWithFromFile:(NSString *) fromFile toFile: (NSString *) toFile;

@end

@interface NSBackgroundTaskHttpRequestToFile : NSObject{
NSString *_url;
NSString *_toFile;
}

@property (nonatomic, retain) id<NSBackgroundTaskCompleteCallback> delegate;

-(void) runTask;

-(id) initWithUrl:(NSString *) url toFile: (NSString *) toFile;

@end

@interface NSBackgroundTaskUnzipTask : NSObject{

NSString *_toFile;
NSString *_fromFile;

}

@property (nonatomic, retain) id<NSBackgroundTaskCompleteCallback> delegate;

-(void) runTask;

-(id) initWithFromFile:(NSString *) fromFile toFile: (NSString *) toFile;

@end
