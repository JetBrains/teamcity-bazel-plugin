package devteam.rx

interface Subject<T> : Observable<T>, Observer<T> {}