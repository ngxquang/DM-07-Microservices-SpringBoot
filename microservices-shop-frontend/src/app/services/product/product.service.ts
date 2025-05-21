import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, of, tap} from "rxjs";
import {Product} from "../../model/product";

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(private httpClient: HttpClient) {
  }

//   getProducts(): Observable<Array<Product>> {
//     return this.httpClient.get<Array<Product>>('http://localhost:9000/api/product');
//   }

  getProducts(): Observable<Array<Product>> {
        const cacheKey = 'productCache';
        const cacheStr = localStorage.getItem(cacheKey);

        if (cacheStr) {
          const { data, expiry } = JSON.parse(cacheStr);
          if (Date.now() < expiry) {
            return of(data);
          } else {
            localStorage.removeItem(cacheKey);
          }
        }

        return this.httpClient.get<Array<Product>>('http://localhost:9000/api/product').pipe(
          tap(data => {
            const expiry = Date.now() + 5 * 60 * 1000; // TTL = 5 phút
            localStorage.setItem(cacheKey, JSON.stringify({ data, expiry }));
          })
        );
      }

  createProduct(product: Product): Observable<Product> {
    return this.httpClient.post<Product>('http://localhost:9000/api/product', product);
  }
}
